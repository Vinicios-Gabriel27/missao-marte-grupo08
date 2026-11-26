package br.edu.unifor.missaomarte.infraestrutura;

import br.edu.unifor.missaomarte.aplicacao.RegistroRanking;
import br.edu.unifor.missaomarte.aplicacao.RepositorioRanking;
import br.edu.unifor.missaomarte.dominio.Dificuldade;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Ranking local em arquivo texto, um registro por linha com campos separados por "|". */
public class RepositorioRankingArquivo implements RepositorioRanking {
    private final Path arquivo;

    public RepositorioRankingArquivo(Path arquivo) {
        this.arquivo = arquivo;
    }

    @Override
    public List<RegistroRanking> listar() {
        List<RegistroRanking> registros = new ArrayList<>();
        if (!Files.exists(arquivo)) {
            return registros;
        }
        try {
            for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
                String[] c = linha.split("\\|", -1);
                if (c.length < 6) {
                    continue;
                }
                try {
                    registros.add(new RegistroRanking(c[0], Integer.parseInt(c[1].trim()), Dificuldade.deTexto(c[2]),
                            Integer.parseInt(c[3].trim()), Long.parseLong(c[5].trim()), c[4]));
                } catch (NumberFormatException linhaCorrompida) {
                    // ignora só a linha ruim, não o ranking inteiro
                }
            }
        } catch (IOException e) {
            return registros;
        }
        return registros;
    }

    @Override
    public void salvarTodos(List<RegistroRanking> registros) {
        List<String> linhas = new ArrayList<>();
        for (RegistroRanking r : registros) {
            linhas.add(limpar(r.piloto()) + "|" + r.pontuacao() + "|" + r.dificuldade().name() + "|"
                    + r.passageirosResgatados() + "|" + r.dataHora() + "|" + r.tempoSegundos());
        }
        try {
            if (arquivo.getParent() != null) {
                Files.createDirectories(arquivo.getParent());
            }
            Files.write(arquivo, linhas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível salvar o ranking", e);
        }
    }

    @Override
    public void limpar() {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível limpar o ranking", e);
        }
    }

    private static String limpar(String texto) {
        return texto.replace("|", " ").replace("\n", " ").replace("\r", " ");
    }
}
