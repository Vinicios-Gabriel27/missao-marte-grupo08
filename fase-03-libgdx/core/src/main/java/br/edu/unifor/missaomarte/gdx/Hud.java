package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.EstadoPartida;
import br.edu.unifor.missaomarte.sobrevivencia.Evento;
import br.edu.unifor.missaomarte.sobrevivencia.Nave;
import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import br.edu.unifor.missaomarte.sobrevivencia.Passageiro;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Arma;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.SorteadorUpgrades;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.Upgrade;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import java.util.List;

/** Interface 2D sobre o 3D: vida, XP, tempo, passageiros, armas, avisos e escolha de upgrade. */
class Hud {
    private static final float DURACAO_AVISO = 2.5f;

    private final MissaoMarteJogo jogo;
    private final PartidaSobrevivencia partida;
    private final Matrix4 projecao = new Matrix4();
    private final Rectangle[] cartoes = {new Rectangle(), new Rectangle(), new Rectangle()};
    private String aviso = "";
    private Color corAviso = Color.WHITE;
    private float tempoAviso;

    Hud(MissaoMarteJogo jogo, PartidaSobrevivencia partida) {
        this.jogo = jogo;
        this.partida = partida;
    }

    void notificar(Evento evento) {
        switch (evento) {
            case PASSAGEIRO_APARECEU -> avisar("Um passageiro apareceu! Siga a seta amarela.", Color.YELLOW);
            case PASSAGEIRO_RESGATADO -> {
                List<Passageiro> bordo = partida.nave().passageirosABordo();
                Passageiro p = bordo.get(bordo.size() - 1);
                avisar(p.nome() + " resgatado! " + p.descricaoBonus(), Color.CYAN);
            }
            case PLATAFORMA_ABERTA -> avisar("PLATAFORMA ABERTA! Volte para o centro!", Color.GREEN);
            case CHEFE_APARECEU -> avisar("O CHEFE guarda a plataforma!", Color.RED);
            default -> { }
        }
    }

    private void avisar(String texto, Color cor) {
        aviso = texto;
        corAviso = cor;
        tempoAviso = DURACAO_AVISO;
    }

    void desenhar(boolean pausado) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        projecao.setToOrtho2D(0, 0, w, h);
        tempoAviso -= Gdx.graphics.getDeltaTime();

        ShapeRenderer f = jogo.formas();
        f.setProjectionMatrix(projecao);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        f.begin(ShapeRenderer.ShapeType.Filled);
        // XP (topo, largura toda)
        f.setColor(0, 0, 0, 0.5f);
        f.rect(0, h - 14, w, 14);
        f.setColor(0.3f, 0.6f, 1f, 1f);
        f.rect(0, h - 14, w * partida.xp() / partida.xpParaProximoNivel(), 14);
        // Vida
        Nave nave = partida.nave();
        f.setColor(0, 0, 0, 0.5f);
        f.rect(20, h - 52, 300, 24);
        f.setColor(0.9f, 0.15f, 0.15f, 1f);
        f.rect(20, h - 52, 300 * nave.vida() / nave.atributos().vidaMaxima(), 24);
        boolean escolhendo = partida.estado() == EstadoPartida.ESCOLHENDO_UPGRADE;
        if (escolhendo || pausado) {
            f.setColor(0, 0, 0, 0.6f);
            f.rect(0, 0, w, h);
        }
        if (escolhendo) {
            desenharFundoCartoes(f, w, h);
        }
        f.end();

        SpriteBatch b = jogo.batch();
        BitmapFont fonte = jogo.fonte();
        b.setProjectionMatrix(projecao);
        b.begin();
        Texto.escrever(b, fonte, "Nível " + partida.nivel(), w - 20, h - 22, 1.4f, Color.WHITE, Align.right);
        Texto.escrever(b, fonte, String.format("%d / %d", (int) nave.vida(), (int) nave.atributos().vidaMaxima()),
                170, h - 33, 1.2f, Color.WHITE, Align.center);
        int t = (int) partida.tempo();
        Texto.centro(b, fonte, String.format("%d:%02d", t / 60, t % 60), w / 2, h - 26, 2f, Color.WHITE);
        String objetivo;
        if (partida.plataformaAberta()) {
            objetivo = "Plataforma aberta: vá para o centro!";
        } else {
            int r = (int) Math.ceil(partida.tempoRestanteParaPlataforma());
            objetivo = r > 0 ? String.format("Plataforma abre em %d:%02d", r / 60, r % 60) : "Resgate todos os passageiros!";
        }
        Texto.centro(b, fonte, objetivo, w / 2, h - 60, 1.2f, partida.plataformaAberta() ? Color.GREEN : Color.LIGHT_GRAY);
        Texto.escrever(b, fonte, "Passageiros: " + partida.passageirosResgatados() + "/" + partida.totalPassageiros(),
                w - 20, h - 50, 1.3f, Color.CYAN, Align.right);
        Texto.escrever(b, fonte, "Abates: " + partida.inimigosDerrotados() + "   Pontos: " + partida.pontuacao(),
                w - 20, h - 75, 1.1f, Color.LIGHT_GRAY, Align.right);

        float y = 30 + 22 * partida.arsenal().size();
        Texto.escrever(b, fonte, "ARMAS", 20, y + 22, 1.1f, Color.ORANGE, Align.left);
        for (Arma a : partida.arsenal()) {
            Texto.escrever(b, fonte, a.nome() + "  nv " + a.nivel() + (a.noNivelMaximo() ? " (máx)" : ""),
                    20, y, 1.1f, Color.WHITE, Align.left);
            y -= 22;
        }
        List<Passageiro> bordo = nave.passageirosABordo();
        float yp = 30 + 22 * bordo.size();
        if (!bordo.isEmpty()) {
            Texto.escrever(b, fonte, "A BORDO", w - 20, yp + 22, 1.1f, Color.CYAN, Align.right);
        }
        for (Passageiro p : bordo) {
            Texto.escrever(b, fonte, p.nome() + "  " + p.descricaoBonus(), w - 20, yp, 1.1f, Color.WHITE, Align.right);
            yp -= 22;
        }
        if (tempoAviso > 0) {
            Color c = new Color(corAviso).mul(1, 1, 1, Math.min(1f, tempoAviso));
            Texto.centro(b, fonte, aviso, w / 2, h * 0.72f, 1.8f, c);
        }
        if (escolhendo) {
            desenharTextoCartoes(b, fonte, w, h);
        }
        if (pausado) {
            Texto.centro(b, fonte, "PAUSADO", w / 2, h / 2 + 30, 3f, Color.WHITE);
            Texto.centro(b, fonte, "P: continuar    Q: voltar ao menu", w / 2, h / 2 - 20, 1.4f, Color.LIGHT_GRAY);
        }
        b.end();
    }

    private void desenharFundoCartoes(ShapeRenderer f, float w, float h) {
        int n = partida.opcoesUpgrade().size();
        float largura = Math.min(320, (w - 80) / 3f);
        float altura = 170;
        float total = n * largura + (n - 1) * 24;
        float x0 = (w - total) / 2;
        for (int i = 0; i < n; i++) {
            Rectangle r = cartoes[i].set(x0 + i * (largura + 24), h / 2 - altura / 2, largura, altura);
            f.setColor(0.12f, 0.1f, 0.2f, 0.95f);
            f.rect(r.x, r.y, r.width, r.height);
            f.setColor(0.6f, 0.45f, 1f, 1f);
            f.rect(r.x, r.y + r.height - 6, r.width, 6);
        }
    }

    private void desenharTextoCartoes(SpriteBatch b, BitmapFont fonte, float w, float h) {
        Texto.centro(b, fonte, "SUBIU DE NÍVEL! Escolha um upgrade", w / 2, h / 2 + 140, 2.2f, Color.YELLOW);
        List<Upgrade> opcoes = partida.opcoesUpgrade();
        for (int i = 0; i < opcoes.size(); i++) {
            Rectangle r = cartoes[i];
            Upgrade u = opcoes.get(i);
            Texto.centro(b, fonte, "[" + (i + 1) + "]", r.x + r.width / 2, r.y + r.height - 20, 1.4f, Color.GRAY);
            Texto.centro(b, fonte, SorteadorUpgrades.tituloPara(u, partida), r.x + r.width / 2, r.y + r.height - 60,
                    1.5f, Color.WHITE);
            Texto.centro(b, fonte, u.descricao(), r.x + r.width / 2, r.y + 50, 1.05f, Color.LIGHT_GRAY);
        }
    }

    /** Índice do cartão clicado/tocado neste quadro, ou -1. */
    int cartaoClicado() {
        if (!Gdx.input.justTouched()) {
            return -1;
        }
        float x = Gdx.input.getX();
        float y = Gdx.graphics.getHeight() - Gdx.input.getY();
        for (int i = 0; i < partida.opcoesUpgrade().size(); i++) {
            if (cartoes[i].contains(x, y)) {
                return i;
            }
        }
        return -1;
    }
}
