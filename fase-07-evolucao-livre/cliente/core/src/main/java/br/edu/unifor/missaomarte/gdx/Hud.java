package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.EstadoPartida;
import br.edu.unifor.missaomarte.sobrevivencia.Evento;
import br.edu.unifor.missaomarte.sobrevivencia.Inimigo;
import br.edu.unifor.missaomarte.sobrevivencia.Nave;
import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import br.edu.unifor.missaomarte.sobrevivencia.Passageiro;
import br.edu.unifor.missaomarte.sobrevivencia.TipoInimigo;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Arma;
import br.edu.unifor.missaomarte.sobrevivencia.PoderEspecial;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.OpcaoUpgrade;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.Raridade;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
    private final Rectangle botaoMenuPausa = new Rectangle();

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
            case NAVE_CURADA -> avisar("VIDA RECUPERADA!", new Color(0.35f, 1f, 0.5f, 1f));
            case EVOLUCAO -> {
                var ev = partida.evolucoes();
                avisar("CARTA CRUEL: " + ev.get(ev.size() - 1).titulo() + "!", new Color(1f, 0.3f, 0.2f, 1f));
            }
            default -> { }
        }
    }

    void lendaria(String titulo) {
        avisar("LENDÁRIA!  " + titulo, new Color(1f, 0.8f, 0.2f, 1f));
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

    void desenhar(boolean pausado, float delta, Camera camera, ControleToque toque) {
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
        // controles de toque: botão de pausa (no painel do tempo) e joystick
        toque.botaoPausa.set(w / 2 - 170, h - 104, 340, 66);
        boolean mostrarToque = ControleToque.telaDeToque();
        if (mostrarToque && !escolhendo && !pausado) {
            f.setColor(1f, 1f, 1f, 0.5f);
            f.rect(w / 2 + 132, h - 82, 6, 22);
            f.rect(w / 2 + 144, h - 82, 6, 22);
            if (toque.ativo()) {
                f.setColor(1f, 1f, 1f, 0.12f);
                f.circle(toque.centroX(), toque.centroY(), ControleToque.RAIO, 40);
                f.setColor(1f, 0.6f, 0.25f, 0.55f);
                float dx = toque.atualX() - toque.centroX();
                float dy = toque.atualY() - toque.centroY();
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float k = d > ControleToque.RAIO ? ControleToque.RAIO / d : 1f;
                f.circle(toque.centroX() + dx * k, toque.centroY() + dy * k, 34, 30);
            } else {
                f.setColor(1f, 1f, 1f, 0.07f);
                f.circle(170, 170, ControleToque.RAIO, 40);
                f.setColor(1f, 1f, 1f, 0.12f);
                f.circle(170, 170, 34, 30);
            }
        }
        // poder especial: anel de recarga embaixo, no centro (no celular, também é o botão)
        PoderEspecial poder = partida.poder();
        float cx = mostrarToque ? w - 150 : w / 2;
        float cy = mostrarToque ? 150 : 70;
        float raioPoder = mostrarToque ? 62 : 34;
        toque.botaoPoder.set(cx - raioPoder - 10, cy - raioPoder - 10, 2 * raioPoder + 20, 2 * raioPoder + 20);
        if (!escolhendo && !pausado) {
            float pronto = 1f - poder.recargaRestante() / poder.recargaTotal();
            f.setColor(0, 0, 0, 0.55f);
            f.circle(cx, cy, raioPoder + 6, 40);
            Color corPoder = poder.pronto() ? new Color(0.3f, 0.9f, 1f, 0.9f) : new Color(0.35f, 0.35f, 0.4f, 0.8f);
            f.setColor(corPoder);
            f.arc(cx, cy, raioPoder, 90, 360 * pronto, 40);
            f.setColor(0.08f, 0.05f, 0.06f, 0.95f);
            f.circle(cx, cy, raioPoder * 0.72f, 36);
            if (poder.pronto()) {
                float brilho = 0.25f + 0.2f * MathUtils.sin(Gdx.graphics.getFrameId() * 0.15f);
                f.setColor(0.3f, 0.9f, 1f, brilho);
                f.circle(cx, cy, raioPoder * 0.72f, 36);
            }
        }
        // bolinhas de nível das armas (ao lado dos nomes, no canto inferior esquerdo)
        float yArma = 40 + 26 * partida.arsenal().size();
        for (Arma a : partida.arsenal()) {
            for (int k = 0; k < a.nivelMaximo(); k++) {
                float px = 252 + k * 15;
                float py = yArma - 8;
                f.setColor(0, 0, 0, 0.6f);
                f.circle(px, py, 6, 12);
                if (a.evoluida()) {
                    f.setColor(1f, 0.75f, 0.15f, 1f);
                    f.circle(px, py, 5, 12);
                } else if (k < a.nivel()) {
                    f.setColor(0.35f, 0.9f, 1f, 1f);
                    f.circle(px, py, 4.5f, 12);
                }
            }
            yArma -= 26;
        }
        if (escolhendo || pausado) {
            f.setColor(0, 0, 0, 0.62f);
            f.rect(0, 0, w, h);
        }
        if (pausado && mostrarToque) {
            botaoMenuPausa.set(w / 2 - 130, h / 2 - 120, 260, 54);
            Ui.retanguloArredondado(f, botaoMenuPausa.x, botaoMenuPausa.y, botaoMenuPausa.width, botaoMenuPausa.height, 10,
                    new Color(0.95f, 0.45f, 0.12f, 0.9f));
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
            Ui.texto(b, fo.textoPequeno, a.nomeAtual(), 22, y, Align.left, a.evoluida() ? Color.GOLD : Ui.BRANCO_SUAVE);
            y -= 26;
        }
        List<Passageiro> bordo = nave.passageirosABordo();
        // no celular o canto inferior direito é do botão DASH: a lista sobe para acima dele
        float yp = (ControleToque.telaDeToque() ? 250 : 40) + 26 * bordo.size();
        if (!bordo.isEmpty()) {
            Ui.texto(b, fo.subtitulo, "A BORDO", w - 22, yp + 30, Align.right, Ui.CIANO);
        }
        for (Passageiro p : bordo) {
            Ui.texto(b, fo.textoPequeno, p.nome() + "   " + p.descricaoBonus(), w - 22, yp, Align.right, Ui.BRANCO_SUAVE);
            yp -= 26;
        }
        if (!escolhendo && !pausado) {
            boolean celular = ControleToque.telaDeToque();
            float cxp = celular ? w - 150 : w / 2;
            float cyp = celular ? 150 : 70;
            PoderEspecial pd = partida.poder();
            String centro = pd.pronto() ? (celular ? "DASH" : "»") : String.valueOf((int) Math.ceil(pd.recargaRestante()));
            Ui.texto(b, fo.texto, centro, cxp, cyp + fo.texto.getCapHeight() / 2, Align.center,
                    pd.pronto() ? Ui.CIANO : Ui.CINZA);
            if (!celular) {
                Ui.texto(b, fo.textoPequeno, "INVESTIDA  [ESPAÇO]", cxp, cyp - 46, Align.center, pd.pronto() ? Ui.CIANO : Ui.CINZA);
            }
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
            if (ControleToque.telaDeToque()) {
                Ui.texto(b, fo.texto, "Toque no relógio para continuar", w / 2, h / 2 - 30, Align.center, Ui.CINZA);
                Ui.texto(b, fo.texto, "MENU PRINCIPAL", w / 2, h / 2 - 84, Align.center, Color.WHITE);
            } else {
                Ui.texto(b, fo.texto, "P: continuar     Q: menu     M: som", w / 2, h / 2 - 30, Align.center, Ui.CINZA);
            }
        }
        b.end();
    }

    private static final float CARTA_L = 236;
    private static final float CARTA_A = 336;

    private void desenharFundoCartoes(ShapeRenderer f, float w, float h) {
        int n = partida.opcoesUpgrade().size();
        float total = n * CARTA_L + (n - 1) * 34;
        float x0 = (w - total) / 2;
        // raios de luz atrás das cartas especiais (antes de todas, para não cobrir as vizinhas)
        for (int i = 0; i < n && tempoEscolha > 0.3f; i++) {
            OpcaoUpgrade u = partida.opcoesUpgrade().get(i);
            if (u.ehEvolucao() || u.raridade() == Raridade.LENDARIO) {
                desenharRaios(f, cartoes[i], u.ehEvolucao() ? new Color(1f, 0.25f, 0.15f, 1f) : new Color(1f, 0.8f, 0.25f, 1f), i);
            }
        }
        for (int i = 0; i < n; i++) {
            OpcaoUpgrade u = partida.opcoesUpgrade().get(i);
            float atraso = Math.max(0, Math.min(1, (tempoEscolha - i * 0.08f) * 4.5f));
            float sobe = (1 - Interpolation.swingOut.apply(atraso)) * -80;
            Rectangle r = cartoes[i].set(x0 + i * (CARTA_L + 34), h / 2 - CARTA_A / 2 - 30 + sobe, CARTA_L, CARTA_A);
            boolean sobre = Ui.mouseSobre(r);
            float flutua = sobre ? 10 : 0;
            r.y += flutua;
            Color cor = corDoUpgrade(u);
            boolean cruel = u.ehEvolucao();
            boolean especial = cruel || u.raridade() == Raridade.LENDARIO;
            // brilho externo (pulsa nas cartas cruel e lendária, em duas camadas)
            float pulso = especial ? 0.35f + 0.25f * MathUtils.sin(tempoEscolha * 6f) : (sobre ? 0.4f : 0.18f);
            if (especial) {
                Ui.retanguloArredondado(f, r.x - 16, r.y - 16, r.width + 32, r.height + 32, 28, new Color(cor.r, cor.g, cor.b, pulso * 0.45f));
            }
            Ui.retanguloArredondado(f, r.x - 8, r.y - 8, r.width + 16, r.height + 16, 22, new Color(cor.r, cor.g, cor.b, pulso));
            // moldura e corpo
            Ui.retanguloArredondado(f, r.x, r.y, r.width, r.height, 18, cor);
            Color corpo = cruel ? new Color(0.22f, 0.03f, 0.05f, 1f) : new Color(0.1f, 0.07f, 0.09f, 1f);
            Ui.retanguloArredondado(f, r.x + 6, r.y + 6, r.width - 12, r.height - 12, 14, corpo);
            // faixa do topo (raridade) e moldura do ícone
            Ui.retanguloArredondado(f, r.x + 6, r.y + r.height - 44, r.width - 12, 38, 12, new Color(cor.r, cor.g, cor.b, 0.85f));
            if (especial) {
                // reflexo de luz passando pela carta de tempos em tempos
                float ciclo = (tempoEscolha * 0.6f + i * 0.3f) % 1.6f;
                float faixaX = r.x - 60 + ciclo / 1.6f * (r.width + 120);
                for (int k = 0; k < 6; k++) {
                    float xs = faixaX + k * 6;
                    float x1 = Math.max(r.x + 8, xs);
                    float x2 = Math.min(r.x + r.width - 8, xs + 6);
                    if (x2 > x1) {
                        f.setColor(1f, 1f, 1f, 0.05f + 0.025f * (3 - Math.abs(k - 2.5f)));
                        f.rect(x1, r.y + 8, x2 - x1, r.height - 16);
                    }
                }
                desenharBrilhos(f, r, i);
            }
            float icx = r.x + r.width / 2;
            float icy = r.y + 244;
            f.setColor(cor.r * 0.35f, cor.g * 0.35f, cor.b * 0.35f, 1f);
            f.circle(icx, icy, 50, 40);
            f.setColor(0.04f, 0.03f, 0.04f, 1f);
            f.circle(icx, icy, 45, 40);
            // bolinhas de nível
            int max = u.nivelMaximo();
            if (!cruel) {
                int atual = u.nivelAtual(partida);
                int depois = u.nivelDepois(partida);
                float passo = 26;
                float bx = icx - (max - 1) * passo / 2;
                for (int k = 0; k < max; k++) {
                    float cx = bx + k * passo;
                    float cy = r.y + 124;
                    f.setColor(0, 0, 0, 0.8f);
                    f.circle(cx, cy, 9, 16);
                    if (k < atual) {
                        f.setColor(cor);
                        f.circle(cx, cy, 7, 16);
                    } else if (k < depois) {
                        float piscar = 0.55f + 0.45f * MathUtils.sin(tempoEscolha * 8f);
                        f.setColor(1f, 1f, 1f, piscar);
                        f.circle(cx, cy, 7, 16);
                    }
                }
            }
        }
    }

    /** Leque de raios de luz girando atrás da carta. */
    private void desenharRaios(ShapeRenderer f, Rectangle r, Color cor, int i) {
        float cx = r.x + r.width / 2;
        float cy = r.y + r.height / 2;
        float alcance = r.height * 0.95f;
        int n = 12;
        for (int k = 0; k < n; k++) {
            float a = (k / (float) n) * MathUtils.PI2 + tempoEscolha * 0.5f + i;
            float abertura = 0.11f;
            f.setColor(cor.r, cor.g, cor.b, 0.13f + 0.05f * MathUtils.sin(tempoEscolha * 3f + k));
            f.triangle(cx, cy,
                    cx + MathUtils.cos(a - abertura) * alcance, cy + MathUtils.sin(a - abertura) * alcance,
                    cx + MathUtils.cos(a + abertura) * alcance, cy + MathUtils.sin(a + abertura) * alcance);
        }
    }

    /** Estrelinhas de quatro pontas correndo pela borda da carta. */
    private void desenharBrilhos(ShapeRenderer f, Rectangle r, int i) {
        float perimetro = 2 * (r.width + r.height);
        for (int k = 0; k < 8; k++) {
            float d = ((tempoEscolha * 90f + k * perimetro / 8f + i * 40f) % perimetro + perimetro) % perimetro;
            float x;
            float y;
            if (d < r.width) {
                x = r.x + d;
                y = r.y;
            } else if (d < r.width + r.height) {
                x = r.x + r.width;
                y = r.y + d - r.width;
            } else if (d < 2 * r.width + r.height) {
                x = r.x + r.width - (d - r.width - r.height);
                y = r.y + r.height;
            } else {
                x = r.x;
                y = r.y + r.height - (d - 2 * r.width - r.height);
            }
            float t = 5f + 3f * MathUtils.sin(tempoEscolha * 8f + k);
            f.setColor(1f, 1f, 0.85f, 0.95f);
            f.triangle(x - t, y, x + t, y, x, y + t * 2.2f);
            f.triangle(x - t, y, x + t, y, x, y - t * 2.2f);
            f.triangle(x, y - t, x, y + t, x + t * 2.2f, y);
            f.triangle(x, y - t, x, y + t, x - t * 2.2f, y);
        }
    }

    /** Cor da raridade (cinza, azul, roxo, dourado) ou vermelho-sangue na carta cruel. */
    static Color corDaRaridade(Raridade r) {
        return switch (r) {
            case COMUM -> new Color(0.62f, 0.6f, 0.6f, 1f);
            case RARO -> new Color(0.25f, 0.6f, 1f, 1f);
            case EPICO -> new Color(0.72f, 0.35f, 1f, 1f);
            case LENDARIO -> new Color(1f, 0.75f, 0.12f, 1f);
        };
    }

    private Color corDoUpgrade(OpcaoUpgrade u) {
        return u.ehEvolucao() ? new Color(0.95f, 0.15f, 0.12f, 1f) : corDaRaridade(u.raridade());
    }

    private void desenharTextoCartoes(SpriteBatch b, Fontes fo, float w, float h) {
        Ui.texto(b, fo.titulo, "SUBIU DE NÍVEL!", w / 2, h / 2 + 222, Align.center, Color.GOLD);
        Ui.texto(b, fo.textoPequeno, "Escolha uma carta  (1, 2, 3 ou toque)", w / 2, h / 2 + 182, Align.center, Ui.CINZA);
        List<OpcaoUpgrade> opcoes = partida.opcoesUpgrade();
        for (int i = 0; i < opcoes.size(); i++) {
            Rectangle r = cartoes[i];
            OpcaoUpgrade u = opcoes.get(i);
            boolean cruel = u.ehEvolucao();
            float cx = r.x + r.width / 2;
            float interno = r.width - 28;
            String faixa = cruel ? "CARTA CRUEL" : u.raridade().toString().toUpperCase() + (u.ehArma() ? "  •  ARMA" : "");
            Ui.textoAjustado(b, fo.subtitulo, faixa, cx, r.y + r.height - 18, interno, Align.center, Color.WHITE);
            Texture icone = jogo.icones().de(u.chave());
            if (icone != null) {
                boolean pulsa = cruel || u.raridade() == Raridade.LENDARIO;
                float lado = 80 + (pulsa ? 6 * MathUtils.sin(tempoEscolha * 5f) : 0);
                b.setColor(Color.WHITE);
                b.draw(icone, cx - lado / 2, r.y + 244 - lado / 2, lado, lado);
            }
            Ui.textoAjustado(b, fo.texto, u.titulo(partida), cx, r.y + 186, interno, Align.center,
                    cruel || u.raridade() == Raridade.LENDARIO ? Color.GOLD : Ui.BRANCO_SUAVE);
            if (cruel) {
                Ui.texto(b, fo.textoPequeno, "EVOLUÇÃO", cx, r.y + 156, Align.center, new Color(1f, 0.45f, 0.4f, 1f));
            } else {
                int depois = u.nivelDepois(partida);
                Ui.texto(b, fo.textoPequeno, u.nivelAtual(partida) == 0 ? "NOVA" : "Nível " + depois,
                        cx, r.y + 156, Align.center, Ui.CINZA);
            }
            Ui.textoQuebrado(b, fo.textoPequeno, u.descricao(), r.x + 14, r.y + 108, interno, Align.center, Ui.CINZA);
            Ui.texto(b, fo.textoPequeno, "[" + (i + 1) + "]", cx, r.y + 22, Align.center, new Color(1, 1, 1, 0.5f));
        }
    }

    boolean toqueMenuNaPausa() {
        return ControleToque.telaDeToque() && Ui.mouseSobre(botaoMenuPausa);
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
