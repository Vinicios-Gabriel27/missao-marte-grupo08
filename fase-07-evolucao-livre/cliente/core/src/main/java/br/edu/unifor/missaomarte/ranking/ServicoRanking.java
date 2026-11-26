package br.edu.unifor.missaomarte.ranking;

import br.edu.unifor.missaomarte.sobrevivencia.EstadoPartida;
import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Regras do ranking local: Top 10 por pontuação; vitórias e derrotas entram (modo sobrevivência). */
public class ServicoRanking {
    public static final int LIMITE = 10;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Comparator<RegistroRanking> ORDEM =
            Comparator.comparingInt(RegistroRanking::pontuacao).reversed()
                    .thenComparingInt(RegistroRanking::tempoSegundos);

    private final RepositorioRanking repositorio;

    public ServicoRanking(RepositorioRanking repositorio) {
        this.repositorio = repositorio;
    }

    public List<RegistroRanking> top() {
        List<RegistroRanking> todos = new ArrayList<>(repositorio.carregar());
        todos.sort(ORDEM);
        return List.copyOf(todos.subList(0, Math.min(LIMITE, todos.size())));
    }

    /**
     * Registra a partida encerrada. Retorna a posição alcançada (1 = primeiro) ou -1 se não entrou.
     */
    public int registrar(String piloto, PartidaSobrevivencia partida) {
        if (!partida.estado().encerrada() || partida.pontuacao() <= 0) {
            return -1;
        }
        RegistroRanking novo = new RegistroRanking(normalizar(piloto), partida.pontuacao(),
                partida.dificuldade().toString(), (int) partida.tempo(), partida.nivel(), partida.inimigosDerrotados(),
                partida.passageirosResgatados(), partida.estado() == EstadoPartida.VITORIA,
                LocalDateTime.now().format(FORMATO));
        List<RegistroRanking> lista = new ArrayList<>(top());
        lista.add(novo);
        lista.sort(ORDEM);
        int posicao = lista.indexOf(novo);
        if (posicao >= LIMITE) {
            return -1;
        }
        repositorio.salvar(lista.subList(0, Math.min(LIMITE, lista.size())));
        return posicao + 1;
    }

    public int recorde() {
        List<RegistroRanking> t = top();
        return t.isEmpty() ? 0 : t.get(0).pontuacao();
    }

    static String normalizar(String piloto) {
        if (piloto == null || piloto.isBlank()) {
            return "Piloto";
        }
        String limpo = piloto.trim();
        return limpo.length() > 16 ? limpo.substring(0, 16) : limpo;
    }
}
