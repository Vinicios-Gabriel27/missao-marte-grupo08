package br.edu.unifor.missaomarte.aplicacao;

import br.edu.unifor.missaomarte.dominio.EstadoPartida;
import br.edu.unifor.missaomarte.dominio.Partida;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Regras do ranking Top 5: quem entra, ordenação e recorde. Antes ficavam no
 * JogoService e no RankingService (que gravava E decidia o corte); agora o
 * repositório só guarda e este serviço decide.
 */
public class ServicoRanking {
    public static final int LIMITE = 5;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RepositorioRanking repositorio;

    public ServicoRanking(RepositorioRanking repositorio) {
        this.repositorio = repositorio;
    }

    public List<RegistroRanking> top() {
        List<RegistroRanking> todos = new ArrayList<>(repositorio.listar());
        todos.sort(Comparator.comparingInt(RegistroRanking::pontuacao).reversed());
        return todos.size() > LIMITE ? List.copyOf(todos.subList(0, LIMITE)) : List.copyOf(todos);
    }

    public Optional<RegistroRanking> recorde() {
        return top().stream().findFirst();
    }

    public boolean entraNoTop(int pontuacao) {
        if (pontuacao <= 0) {
            return false;
        }
        List<RegistroRanking> atual = top();
        return atual.size() < LIMITE || pontuacao > atual.get(atual.size() - 1).pontuacao();
    }

    /** Registra uma partida vencida, se ela entrar no Top 5. Retorna true se entrou. */
    public boolean registrar(String piloto, Partida partida, long tempoSegundos) {
        if (partida.getEstado() != EstadoPartida.VITORIA || !entraNoTop(partida.getPontuacao())) {
            return false;
        }
        List<RegistroRanking> novo = new ArrayList<>(top());
        novo.add(new RegistroRanking(normalizarNome(piloto), partida.getPontuacao(), partida.getDificuldade(),
                partida.getPassageirosResgatados(), tempoSegundos, LocalDateTime.now().format(FORMATO)));
        novo.sort(Comparator.comparingInt(RegistroRanking::pontuacao).reversed());
        repositorio.salvarTodos(novo.size() > LIMITE ? novo.subList(0, LIMITE) : novo);
        return true;
    }

    public void limpar() {
        repositorio.limpar();
    }

    static String normalizarNome(String nome) {
        return (nome == null || nome.isBlank()) ? "Piloto Anônimo" : nome.trim();
    }
}
