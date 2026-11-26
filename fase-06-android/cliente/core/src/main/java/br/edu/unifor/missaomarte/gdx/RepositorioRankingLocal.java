package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.ranking.RegistroRanking;
import br.edu.unifor.missaomarte.ranking.RepositorioRanking;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.ArrayList;
import java.util.List;

/** Ranking em arquivo JSON local (pasta do jogo no desktop, armazenamento interno no Android). */
class RepositorioRankingLocal implements RepositorioRanking {
    /** Formato do arquivo (a libGDX serializa campos públicos de classes simples). */
    public static class Linha {
        public String piloto;
        public int pontuacao;
        public String dificuldade;
        public int tempoSegundos;
        public int nivel;
        public int abates;
        public int passageiros;
        public boolean venceu;
        public String dataHora;
    }

    private final FileHandle arquivo = Gdx.files.local("missao-marte-ranking.json");
    private final Json json = new Json(JsonWriter.OutputType.json);

    @Override
    @SuppressWarnings("unchecked")
    public List<RegistroRanking> carregar() {
        List<RegistroRanking> lista = new ArrayList<>();
        if (!arquivo.exists()) {
            return lista;
        }
        try {
            Array<Linha> linhas = json.fromJson(Array.class, Linha.class, arquivo);
            for (Linha l : linhas) {
                lista.add(new RegistroRanking(l.piloto, l.pontuacao, l.dificuldade, l.tempoSegundos, l.nivel,
                        l.abates, l.passageiros, l.venceu, l.dataHora));
            }
        } catch (RuntimeException arquivoCorrompido) {
            Gdx.app.error("Ranking", "Arquivo de ranking ilegível; começando vazio", arquivoCorrompido);
        }
        return lista;
    }

    @Override
    public void salvar(List<RegistroRanking> registros) {
        Array<Linha> linhas = new Array<>();
        for (RegistroRanking r : registros) {
            Linha l = new Linha();
            l.piloto = r.piloto();
            l.pontuacao = r.pontuacao();
            l.dificuldade = r.dificuldade();
            l.tempoSegundos = r.tempoSegundos();
            l.nivel = r.nivel();
            l.abates = r.abates();
            l.passageiros = r.passageiros();
            l.venceu = r.venceu();
            l.dataHora = r.dataHora();
            linhas.add(l);
        }
        arquivo.writeString(json.prettyPrint(json.toJson(linhas, Array.class, Linha.class)), false, "UTF-8");
    }
}
