package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.EstadoPartida;
import br.edu.unifor.missaomarte.sobrevivencia.Evento;
import br.edu.unifor.missaomarte.sobrevivencia.Inimigo;
import br.edu.unifor.missaomarte.sobrevivencia.Nave;
import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import br.edu.unifor.missaomarte.sobrevivencia.Passageiro;
import br.edu.unifor.missaomarte.sobrevivencia.TipoInimigo;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Arma;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.SorteadorUpgrades;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.Upgrade;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import java.util.List;

/** Interface 2D sobre o 3D: vida, XP, tempo, objetivo, chefe, armas, avisos, dano e upgrades. */
class Hud {
    private static final float DURACAO_AVISO = 2.8f;

    private static final class NumeroDano {
        final Vector3 mundo = new Vector3();
        String texto;
        float vida;
        boolean grande;
    }

    private final MissaoMarteJogo jogo;
    private final PartidaSobrevivencia partida;
    private final Rectangle[] cartoes = {new Rectangle(), new Rectangle(), new Rectangle()};
    private final Array<NumeroDano> numeros = new Array<>();
    private final Vector3 tela = new Vector3();
    private String aviso = "";
    private Color corAviso = Color.WHITE;
    private float tempoAviso;
    private float vidaExibida = -1;
    private float xpExibido;
    private float tempoEscolha;

    Hud(MissaoMarteJogo jogo, PartidaSobrevivencia partida) {
        this.jogo = jogo;
        this.partida = partida;
    }

    void notificar(Evento evento) {
        switch (evento) {
            case PASSAGEIRO_APARECEU -> avisar("Passageiro avistado! Siga a seta", Color.GOLD);
            case PASSAGEIRO_RESGATADO -> {
                List<Passageiro> bordo = partida.nave().passageirosABordo();
                Passageiro p = bordo.get(bordo.size() - 1);
                avisar(p.nome() + " a bordo!  " + p.descricaoBonus(), Ui.CIANO);
            }
            case PLATAFORMA_ABERTA -> avisar("PLATAFORMA ABERTA!  Volte ao centro", new Color(0.4f, 1f, 0.5f, 1f));
            case CHEFE_APARECEU -> avisar("O GUARDIÃO DESPERTOU!", new Color(1f, 0.3f, 0.25f, 1f));
            case SUBIU_NIVEL -> tempoEscolha = 0;
            default -> { }
        }
    }

    private void avisar(String texto, Color cor) {
        aviso = texto;
        corAviso = cor;
        tempoAviso = DURACAO_AVISO;
    }

    void numeroDano(float x, float z, int valor, boolean grande) {
        NumeroDano n = new NumeroDano();
        n.mundo.set(x + MathUtils.random(-0.3f, 0.3f), grande ? 5f : 1.8f, z);
        n.texto = String.valueOf(valor);
        n.vida = 0.8f;
        n.grande = grande;
        numeros.add(n);
    }

    void desenhar(boolean pausado, float delta, Camera camera) {
        float w = Ui.largura();
        float h = Ui.ALTURA;
        tempoAviso -= delta;
        tempoEscolha += delta;
        Nave nave = partida.nave();
        float vidaMax = nave.atributos().vidaMaxima();
        vidaExibida = vidaExibida < 0 ? nave.vida() : MathUtils.lerp(vidaExibida, nave.vida(), Math.min(1, delta * 8));
        float fracaoXp = partida.xp() / partida.xpParaProximoNivel();
        xpExibido = fracaoXp < xpExibido ? fracaoXp : MathUtils.lerp(xpExibido, fracaoXp, Math.min(1, delta * 10));
        Inimigo chefe = partida.inimigos().stream().filter(i -> i.tipo() == TipoInimigo.CHEFE).findFirst().orElse(null);
        boolean escolhendo = partida.estado() == EstadoPartida.ESCOLHENDO_UPGRADE;

        ShapeRenderer f = jogo.formas();
        Ui.comecarFormas(f);
        // XP (topo)
        Ui.barra(f, 16, h - 26, w - 32, 12, xpExibido, new Color(0.3f, 0.75f, 1f, 1f));
        // Vida
        Ui.painel(f, 16, h - 98, 330, 58, new Color(1f, 0.3f, 0.25f, 1f));
        boolean critico = nave.vida() / vidaMax < 0.3f;
        Color corVida = critico && (int) (Gdx.graphics.getFrameId() / 8) % 2 == 0 ? new Color(1f, 0.6f, 0.5f, 1f)
                : new Color(0.95f, 0.22f, 0.2f, 1f);
        Ui.barra(f, 30, h - 84, 300, 16, vidaExibida / vidaMax, corVida);
        // Tempo / objetivo
        Ui.painel(f, w / 2 - 170, h - 104, 340, 66, Ui.LARANJA);
        // Status (direita)
        Ui.painel(f, w - 286, h - 146, 270, 108, Ui.CIANO);
        // Chefe
        if (chefe != null) {
            Ui.painel(f, w / 2 - 300, 26, 600, 52, new Color(1f, 0.2f, 0.2f, 1f));
            Ui.barra(f, w / 2 - 280, 36, 560, 14, chefe.fracaoVida(), new Color(0.85f, 0.1f, 0.15f, 1f));
        }
        if (critico) {
            float a = 0.18f + 0.12f * MathUtils.sin(Gdx.graphics.getFrameId() * 0.2f);
            for (int i = 0; i < 10; i++) {
                f.setColor(0.8f, 0f, 0f, a * (1 - i / 10f));
                f.rect(0, i * 6, w, 6);
                f.rect(0, h - (i + 1) * 6, w, 6);
            }
        }
        if (escolhendo || pausado) {
            f.setColor(0, 0, 0, 0.62f);
            f.rect(0, 0, w, h);
        }
        if (escolhendo) {
            desenharFundoCartoes(f, w, h);
        }
        f.end();

        Fontes fo = jogo.fontes();
        SpriteBatch b = jogo.batch();
        b.setProjectionMatrix(Ui.projecao());
        b.begin();
        // números de dano (projetados do 3D para a tela)
        for (int i = numeros.size - 1; i >= 0; i--) {
            NumeroDano n = numeros.get(i);
            n.vida -= delta;
            n.mundo.y += delta * 1.6f;
            if (n.vida <= 0 || pausado) {
                if (n.vida <= 0) {
                    numeros.removeIndex(i);
                }
                continue;
            }
            tela.set(n.mundo);
            camera.project(tela);
            float x = tela.x * w / Gdx.graphics.getWidth();
            float y = tela.y * h / Gdx.graphics.getHeight();
            Color cor = n.grande ? new Color(1f, 0.85f, 0.2f, Math.min(1, n.vida * 2)) : new Color(1f, 1f, 1f, Math.min(1, n.vida * 2));
            Ui.texto(b, n.grande ? fo.numero : fo.textoPequeno, n.texto, x, y, Align.center, cor);
        }
        Ui.texto(b, fo.textoPequeno, "VIDA", 30, h - 50, Align.left, Ui.CINZA);
        Ui.texto(b, fo.textoPequeno, (int) nave.vida() + " / " + (int) vidaMax, 330, h - 50, Align.right, Ui.BRANCO_SUAVE);
        int t = (int) partida.tempo();
        Ui.texto(b, fo.titulo, String.format("%d:%02d", t / 60, t % 60), w / 2, h - 50, Align.center, Ui.BRANCO_SUAVE);
        String objetivo;
        Color corObjetivo = Ui.CINZA;
        if (partida.plataformaAberta()) {
            objetivo = "Pouse na plataforma!";
            corObjetivo = new Color(0.4f, 1f, 0.5f, 1f);
        } else {
            int r = (int) Math.ceil(partida.tempoRestanteParaPlataforma());
            int faltam = partida.totalPassageiros() - partida.passageirosResgatados();
            objetivo = r > 0 ? String.format("Plataforma abre em %d:%02d", r / 60, r % 60)
                    : "Resgate mais " + faltam + " passageiro" + (faltam == 1 ? "" : "s");
        }
        Ui.texto(b, fo.textoPequeno, objetivo, w / 2, h - 82, Align.center, corObjetivo);
        Ui.texto(b, fo.subtitulo, "NÍVEL " + partida.nivel(), w - 270, h - 54, Align.left, Color.GOLD);
        Ui.texto(b, fo.texto, "Passageiros  " + partida.passageirosResgatados() + "/" + partida.totalPassageiros(),
                w - 270, h - 84, Align.left, Ui.CIANO);
        Ui.texto(b, fo.textoPequeno, "Abates " + partida.inimigosDerrotados() + "    " + partida.pontuacao() + " pts",
                w - 270, h - 116, Align.left, Ui.BRANCO_SUAVE);
        if (chefe != null) {
            Ui.texto(b, fo.subtitulo, "GUARDIÃO DE MARTE", w / 2, 92, Align.center, new Color(1f, 0.4f, 0.35f, 1f));
        }
        // armas e passageiros
        float y = 40 + 26 * partida.arsenal().size();
        Ui.texto(b, fo.subtitulo, "ARMAS", 22, y + 30, Align.left, Ui.LARANJA);
        for (Arma a : partida.arsenal()) {
            String nivel = a.noNivelMaximo() ? "MÁX" : "nv " + a.nivel();
            Ui.texto(b, fo.textoPequeno, a.nome() + "   " + nivel, 22, y, Align.left, Ui.BRANCO_SUAVE);
            y -= 26;
        }
        List<Passageiro> bordo = nave.passageirosABordo();
        float yp = 40 + 26 * bordo.size();
        if (!bordo.isEmpty()) {
            Ui.texto(b, fo.subtitulo, "A BORDO", w - 22, yp + 30, Align.right, Ui.CIANO);
        }
        for (Passageiro p : bordo) {
            Ui.texto(b, fo.textoPequeno, p.nome() + "   " + p.descricaoBonus(), w - 22, yp, Align.right, Ui.BRANCO_SUAVE);
            yp -= 26;
        }
        if (tempoAviso > 0) {
            float entrada = Interpolation.swingOut.apply(Math.min(1f, (DURACAO_AVISO - tempoAviso) * 4f));
            Color c = new Color(corAviso).mul(1, 1, 1, Math.min(1f, tempoAviso * 1.5f));
            fo.titulo.getData().setScale(0.5f * (0.6f + 0.4f * entrada) * 0.75f);
            Ui.texto(b, fo.titulo, aviso, w / 2, h * 0.74f, Align.center, c);
            fo.titulo.getData().setScale(0.5f);
        }
        if (escolhendo) {
            desenharTextoCartoes(b, fo, w, h);
        }
        if (pausado) {
            Ui.texto(b, fo.tituloGigante, "PAUSA", w / 2, h / 2 + 60, Align.center, Ui.BRANCO_SUAVE);
            Ui.texto(b, fo.texto, "P: continuar     Q: menu     M: som", w / 2, h / 2 - 30, Align.center, Ui.CINZA);
        }
        b.end();
    }

    private void desenharFundoCartoes(ShapeRenderer f, float w, float h) {
        int n = partida.opcoesUpgrade().size();
        float largura = 300;
        float altura = 230;
        float total = n * largura + (n - 1) * 30;
        float x0 = (w - total) / 2;
        for (int i = 0; i < n; i++) {
            float atraso = Math.max(0, Math.min(1, (tempoEscolha - i * 0.07f) * 5f));
            float sobe = (1 - Interpolation.swingOut.apply(atraso)) * -60;
            Rectangle r = cartoes[i].set(x0 + i * (largura + 30), h / 2 - altura / 2 - 20 + sobe, largura, altura);
            boolean sobre = Ui.mouseSobre(r);
            Color cor = corDoUpgrade(partida.opcoesUpgrade().get(i));
            if (sobre) {
                Ui.retanguloArredondado(f, r.x - 6, r.y - 6, r.width + 12, r.height + 12, 18, new Color(cor.r, cor.g, cor.b, 0.35f));
            }
            Ui.painel(f, r.x, r.y, r.width, r.height, cor);
            f.setColor(cor.r, cor.g, cor.b, 0.18f);
            f.circle(r.x + r.width / 2, r.y + r.height - 70, 34, 24);
        }
    }

    private Color corDoUpgrade(Upgrade u) {
        return u instanceof br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeArma<?>
                ? new Color(0.75f, 0.45f, 1f, 1f) : Ui.LARANJA;
    }

    private void desenharTextoCartoes(SpriteBatch b, Fontes fo, float w, float h) {
        Ui.texto(b, fo.titulo, "SUBIU DE NÍVEL!", w / 2, h / 2 + 190, Align.center, Color.GOLD);
        Ui.texto(b, fo.textoPequeno, "Escolha um upgrade  (1, 2, 3 ou clique)", w / 2, h / 2 + 145, Align.center, Ui.CINZA);
        List<Upgrade> opcoes = partida.opcoesUpgrade();
        for (int i = 0; i < opcoes.size(); i++) {
            Rectangle r = cartoes[i];
            Upgrade u = opcoes.get(i);
            Ui.texto(b, fo.titulo, String.valueOf(i + 1), r.x + r.width / 2, r.y + r.height - 55, Align.center, corDoUpgrade(u));
            Ui.texto(b, fo.texto, SorteadorUpgrades.tituloPara(u, partida), r.x + r.width / 2, r.y + 100, Align.center, Ui.BRANCO_SUAVE);
            Ui.texto(b, fo.textoPequeno, u.descricao(), r.x + r.width / 2, r.y + 58, Align.center, Ui.CINZA);
        }
    }

    /** Índice do cartão clicado/tocado neste quadro, ou -1. */
    int cartaoClicado() {
        if (!Gdx.input.justTouched()) {
            return -1;
        }
        for (int i = 0; i < partida.opcoesUpgrade().size(); i++) {
            if (Ui.mouseSobre(cartoes[i])) {
                return i;
            }
        }
        return -1;
    }
}
