package br.edu.unifor.missaomarte.infraestrutura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unifor.missaomarte.aplicacao.RegistroRanking;
import br.edu.unifor.missaomarte.dominio.Dificuldade;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepositorioRankingArquivoTest {
    @TempDir
    Path pasta;

    @Test
    void gravaELeOsMesmosRegistros() {
        RepositorioRankingArquivo repo = new RepositorioRankingArquivo(pasta.resolve("ranking.txt"));
        RegistroRanking r = new RegistroRanking("Ana", 42, Dificuldade.DIFICIL, 5, 30, "25/11/2026 20:00");

        repo.salvarTodos(List.of(r));

        assertEquals(List.of(r), repo.listar());
    }

    @Test
    void linhaCorrompidaEIgnorada() throws Exception {
        Path arquivo = pasta.resolve("ranking.txt");
        Files.writeString(arquivo, "lixo\nAna|abc|MEDIO|1|x|2\nBia|10|FACIL|2|01/01/2026 10:00|5\n");

        List<RegistroRanking> lidos = new RepositorioRankingArquivo(arquivo).listar();

        assertEquals(1, lidos.size());
        assertEquals("Bia", lidos.get(0).piloto());
    }

    @Test
    void limparApagaOArquivo() {
        Path arquivo = pasta.resolve("ranking.txt");
        RepositorioRankingArquivo repo = new RepositorioRankingArquivo(arquivo);
        repo.salvarTodos(List.of(new RegistroRanking("A", 1, Dificuldade.MEDIO, 0, 0, "-")));

        repo.limpar();

        assertTrue(repo.listar().isEmpty());
        assertTrue(Files.notExists(arquivo));
    }
}
