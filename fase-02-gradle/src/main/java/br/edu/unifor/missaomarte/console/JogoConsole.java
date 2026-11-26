package br.edu.unifor.missaomarte.console;

import br.edu.unifor.missaomarte.aplicacao.RegistroRanking;
import br.edu.unifor.missaomarte.aplicacao.ServicoRanking;
import br.edu.unifor.missaomarte.dominio.Dificuldade;
import br.edu.unifor.missaomarte.dominio.Direcao;
import br.edu.unifor.missaomarte.dominio.Evento;
import br.edu.unifor.missaomarte.dominio.GeradorMissao;
import br.edu.unifor.missaomarte.dominio.Limites;
import br.edu.unifor.missaomarte.dominio.Nave;
import br.edu.unifor.missaomarte.dominio.Partida;
import br.edu.unifor.missaomarte.dominio.ResultadoTurno;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

/**
 * Adaptador de console: lê o teclado, chama a Partida e traduz eventos em mensagens.
 * Não contém regra de jogo — a mesma Partida será usada pelo libGDX na Fase 3.
 */
public class JogoConsole {
    private final Scanner in;
    private final PrintStream out;
    private final ServicoRanking ranking;
    private final Random random;
    private final MapaConsole mapa;

    public JogoConsole(Scanner in, PrintStream out, ServicoRanking ranking, Random random) {
        this.in = in;
        this.out = out;
        this.ranking = ranking;
        this.random = random;
        this.mapa = new MapaConsole(out);
    }

    public void executar() {
        while (true) {
            out.println("\n--- MENU PRINCIPAL ---");
            out.println("1. Iniciar Nova Missão");
            out.println("2. Visualizar Ranking Top 5");
            out.println("3. Resetar Histórico de Ranking");
            out.println("4. Sair do Jogo");
            out.println("----------------------");
            String opcao = ler("Escolha uma opção: ", "1");
            if (opcao == null) {
                return;
            }
            switch (opcao) {
                case "1" -> jogar();
                case "2" -> mostrarRanking();
                case "3" -> resetarRanking();
                case "4" -> {
                    out.println("\nObrigado por jogar a Missão Marte Unifor!");
                    return;
                }
                default -> out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private void jogar() {
        String piloto = ler("\nDigite o nome do piloto: ", "Piloto Anônimo");
        if (piloto == null) {
            return;
        }
        String textoDificuldade = ler("Escolha a Dificuldade (facil/medio/dificil): ", "medio");
        if (textoDificuldade == null) {
            return;
        }
        Dificuldade dificuldade = Dificuldade.deTexto(textoDificuldade);
        Limites limites = Limites.centrado(lerTamanho());
        out.println("\nIniciando missão na dificuldade " + dificuldade + "...");
        if (ler("Pressione Enter para decolar!", "") == null) {
            return;
        }

        Partida partida = new Partida(new GeradorMissao(random).gerar(dificuldade, limites), dificuldade, random);
        if (partida.getMissao().getPassageiros().size() < dificuldade.passageiros()) {
            out.printf("Aviso: o mapa não coube todos os passageiros (%d/%d).%n",
                    partida.getMissao().getPassageiros().size(), dificuldade.passageiros());
        }
        long inicio = System.currentTimeMillis();

        while (!partida.getEstado().encerrada()) {
            mapa.desenhar(partida, piloto);
            Nave nave = partida.getMissao().getNave();
            int aBordo = nave.getPassageirosABordo().size();
            int restantes = partida.getMissao().getPassageiros().size();
            out.printf("Nave em (%d,%d) | Pontos: %d | Vidas: %d | A bordo: %d/%d | Restantes no mapa: %d | Total: %d%n",
                    nave.getPosicao().x(), nave.getPosicao().y(), partida.getPontuacao(), nave.getVidas(),
                    aBordo, nave.getCapacidade(), restantes, aBordo + restantes);

            String entrada = ler("Comando (w/s/a/d/c/q): ", "");
            if (entrada == null) {
                anunciar(partida.abortar(), partida);
                break;
            }
            if (entrada.isEmpty()) {
                continue;
            }
            Optional<ResultadoTurno> resultado = executarComando(entrada.toLowerCase().charAt(0), partida);
            if (resultado.isEmpty()) {
                out.println("Comando inválido.");
                continue;
            }
            anunciar(resultado.get(), partida);
        }

        if (partida.getEstado() == br.edu.unifor.missaomarte.dominio.EstadoPartida.VITORIA) {
            long segundos = (System.currentTimeMillis() - inicio) / 1000;
            mostrarEstatisticas(partida, segundos);
            if (ranking.registrar(piloto, partida, segundos)) {
                out.println("Parabéns! Você entrou para o Top 5 de pilotos!");
            }
        }
    }

    /** Tradução tecla -> ação. Fica aqui porque teclado é detalhe do console. */
    private Optional<ResultadoTurno> executarComando(char tecla, Partida partida) {
        return switch (tecla) {
            case 'w' -> Optional.of(partida.mover(Direcao.CIMA));
            case 's' -> Optional.of(partida.mover(Direcao.BAIXO));
            case 'a' -> Optional.of(partida.mover(Direcao.ESQUERDA));
            case 'd' -> Optional.of(partida.mover(Direcao.DIREITA));
            case 'c' -> Optional.of(partida.embarcar());
            case 'q' -> Optional.of(partida.abortar());
            default -> Optional.empty();
        };
    }

    private void anunciar(ResultadoTurno resultado, Partida partida) {
        for (Evento evento : resultado.eventos()) {
            switch (evento) {
                case PASSAGEIRO_EMBARCADO -> resultado.embarcado().ifPresent(p -> out.printf(
                        "Passageiro %s embarcado com sucesso! +%d pontos!%n", p.getNome(), p.getPontuacao()));
                case NENHUM_PASSAGEIRO_AQUI -> out.println("Nenhum passageiro nesta posição.");
                case NAVE_CHEIA -> out.println("Nave cheia! Não há espaço para mais passageiros.");
                case COLISAO -> {
                    if (partida.getMissao().getNave().getVidas() > 0) {
                        out.printf("Alerta! Colisão detectada! Vidas restantes: %d%n",
                                partida.getMissao().getNave().getVidas());
                    }
                }
                case NAVE_DESTRUIDA -> out.println("GAME OVER! A nave foi destruída.");
                case COMBUSTIVEL_ESGOTADO -> out.println("Combustível/Pontuação zerada! Missão perdida.");
                case RETORNE_A_PLATAFORMA -> out.println("ALERTA: Todos os passageiros resgatados! "
                        + "Retorne para a Plataforma de Pouso 'L' em (0,0) para completar a missão.");
                case MISSAO_CUMPRIDA -> {
                    out.println("\n================================================================");
                    out.println("DECOLAGEM AUTORIZADA! Nave acoplada à plataforma em (0,0).");
                    out.println("Retornando à órbita marciana com todos os passageiros. Missão cumprida!");
                    out.println("================================================================");
                }
                case MISSAO_ABORTADA -> out.println("Missão abortada pelo piloto.");
                default -> { }
            }
        }
    }

    private void mostrarEstatisticas(Partida partida, long segundos) {
        out.println("Estatísticas da Partida:");
        out.printf(" - Pontuação Final: %d pontos%n", partida.getPontuacao());
        out.printf(" - Movimentos Efetuados: %d%n", partida.getMovimentos());
        out.printf(" - Tempo de Jogo: %d segundos%n", segundos);
        out.printf(" - Passageiros Resgatados: %d%n", partida.getPassageirosResgatados());
        ranking.recorde().ifPresent(r -> {
            if (partida.getPontuacao() > r.pontuacao()) {
                out.println("Novo recorde absoluto do sistema!");
            } else {
                out.printf(" - Recorde atual a ser batido: %d pontos (Piloto: %s)%n", r.pontuacao(), r.piloto());
            }
        });
        out.println("================================================================");
    }

    private void mostrarRanking() {
        out.println("\n====== RANKING TOP 5 PILOTOS ======");
        List<RegistroRanking> top = ranking.top();
        if (top.isEmpty()) {
            out.println(" - Nenhum registro encontrado. Seja o primeiro a jogar!");
        }
        for (int i = 0; i < top.size(); i++) {
            RegistroRanking r = top.get(i);
            out.printf("%d. %s - %d pts | Dificuldade: %s | Coletados: %d | Tempo: %ds | %s%n",
                    i + 1, r.piloto(), r.pontuacao(), r.dificuldade(), r.passageirosResgatados(),
                    r.tempoSegundos(), r.dataHora());
        }
        out.println("===================================");
    }

    private void resetarRanking() {
        String resposta = ler("Você realmente deseja limpar o histórico de ranking? (s/n): ", "n");
        if (resposta != null && (resposta.equalsIgnoreCase("s") || resposta.equalsIgnoreCase("sim"))) {
            try {
                ranking.limpar();
                out.println("Ranking resetado com sucesso!");
            } catch (RuntimeException e) {
                out.println("Erro ao resetar ranking: " + e.getMessage());
            }
        } else {
            out.println("Operação cancelada.");
        }
    }

    private int lerTamanho() {
        String valor = ler("Tamanho do mapa (ex: 5 para mapa de -5 a +5): ", "5");
        try {
            int t = Integer.parseInt(valor == null ? "5" : valor);
            return t > 0 ? t : 5;
        } catch (NumberFormatException e) {
            out.println("Entrada inválida, usando tamanho padrão (5).");
            return 5;
        }
    }

    /** Retorna null no fim da entrada (Ctrl+D/Ctrl+Z); vazio vira o valor padrão. */
    private String ler(String prompt, String padrao) {
        out.print(prompt);
        if (!in.hasNextLine()) {
            return null;
        }
        String valor = in.nextLine().trim();
        return valor.isEmpty() ? padrao : valor;
    }
}
