package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.CristalXp;
import br.edu.unifor.missaomarte.sobrevivencia.Dificuldade;
import br.edu.unifor.missaomarte.sobrevivencia.Engenheiro;
import br.edu.unifor.missaomarte.sobrevivencia.EstadoPartida;
import br.edu.unifor.missaomarte.sobrevivencia.Evento;
import br.edu.unifor.missaomarte.sobrevivencia.Inimigo;
import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import br.edu.unifor.missaomarte.sobrevivencia.Passageiro;
import br.edu.unifor.missaomarte.sobrevivencia.Professor;
import br.edu.unifor.missaomarte.sobrevivencia.Projetil;
import br.edu.unifor.missaomarte.sobrevivencia.TipoInimigo;
import br.edu.unifor.missaomarte.sobrevivencia.Vetor;
import br.edu.unifor.missaomarte.sobrevivencia.armas.OndaDeChoque;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Orbital;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Tela 3D da partida. Lê o controle, chama {@link PartidaSobrevivencia#atualizar}, toca sons
 * e desenha o estado usando o {@link Mundo3D}. Nenhuma regra de jogo fica aqui.
 * Mundo: domínio (x, y) → 3D (x, 0, -y). Assim "cima" no controle afasta da câmera.
 */
public class TelaJogo extends ScreenAdapter {
    private static final Vector3 DESLOCAMENTO_CAMERA = new Vector3(0, 12.5f, 11.5f);

    private final MissaoMarteJogo jogo;
    private final Mundo3D mundo;
    private final PartidaSobrevivencia partida;
    private final Cenario cenario;
    private final Hud hud;
    private final Particulas particulas;
    private final boolean demo = MissaoMarteJogo.modoDemonstracao();

    private final ModelInstance iProjetil;
    private final ModelInstance iCristal;
    private final ModelInstance iOrbital;
    private final ModelInstance iOnda;
    private final ModelInstance iSeta;
    private final ModelInstance iFeixeProfessor;
    private final ModelInstance iFeixeEngenheiro;
    private final ModelInstance iFeixeAstronauta;
    private final ModelInstance iFeixePlataforma;
    private final ModelInstance iAnelAberto;

    /** Último estado de cada inimigo: x, y, vida, pulso. Detecta acerto e morte. */
    private final Map<Inimigo, float[]> rastroInimigos = new IdentityHashMap<>();
    private final Map<Inimigo, Integer> varianteInimigo = new IdentityHashMap<>();
    private int proximaVariante;
    private final Vector3 alvoCamera = new Vector3();
    private float tremor;
    private float relogio;
    private float tempoDesdeFim;
    private float tempoRastroMotor;
    private boolean pausado;
    private boolean naveAndando;
    private int capturas;

    public TelaJogo(MissaoMarteJogo jogo, Dificuldade dificuldade) {
        this.jogo = jogo;
        this.mundo = jogo.mundo();
        long semente = System.nanoTime();
        this.partida = new PartidaSobrevivencia(dificuldade, new Random(semente));
        this.cenario = new Cenario(mundo.modelos, mundo.kenney, partida.arena(), semente);
        this.hud = new Hud(jogo, partida);
        this.particulas = new Particulas(mundo.modelos);
        Modelos m = mundo.modelos;
        iProjetil = new ModelInstance(m.projetil);
        iCristal = new ModelInstance(m.cristal);
        iOrbital = new ModelInstance(m.orbital);
        iOnda = new ModelInstance(m.onda);
        iSeta = new ModelInstance(m.seta);
        iFeixeProfessor = new ModelInstance(m.feixeProfessor);
        iFeixeEngenheiro = new ModelInstance(m.feixeEngenheiro);
        iFeixeAstronauta = new ModelInstance(m.feixeAstronauta);
        iFeixePlataforma = new ModelInstance(m.feixePlataforma);
        iAnelAberto = new ModelInstance(m.anelPlataformaAberta);
        mundo.luzPlataforma.color.set(1f, 0.55f, 0.15f, 1f);
        posicionarCamera(1f);
    }

    @Override
    public void show() {
        jogo.sons().tocarMusica();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 20f);
        relogio += delta;
        jogo.sons().atualizar(delta);
        lerEntradaGeral();
        if (!pausado) {
            Vetor entrada = demo ? pilotoAutomatico() : ControleTeclado.direcao();
            naveAndando = entrada.comprimento() > 0.01f;
            reagir(partida.atualizar(delta, entrada));
        }
        if (partida.estado().encerrada()) {
            tempoDesdeFim += delta;
            if (tempoDesdeFim > 2f) {
                jogo.trocarTela(new TelaFim(jogo, partida));
                return;
            }
        }
        tremor = Math.max(0, tremor - delta * 2.5f);
        posicionarCamera(delta);
        boolean rodando = !pausado && partida.estado() == EstadoPartida.EM_ANDAMENTO;
        if (rodando || partida.estado().encerrada()) {
            mundo.kenney.atualizar(delta);
            acompanharInimigos(delta);
            atualizarRastroMotor(delta);
            particulas.atualizar(delta);
        }
        atualizarLuzes();
        mundo.desenhar(alvoCamera, cenario.instanciasPbr, this::desenharPbr, this::desenharEfeitos);
        hud.desenhar(pausado, delta, mundo.camera);
        if (demo) {
            capturarDemonstracao();
        }
    }

    private void lerEntradaGeral() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pausado = !pausado;
            jogo.sons().tocar("clique", 0.6f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            jogo.sons().alternarMudo();
        }
        if (pausado && Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            jogo.trocarTela(new TelaMenu(jogo));
            return;
        }
        if (partida.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
            int clicado = hud.cartaoClicado();
            for (int i = 0; i < partida.opcoesUpgrade().size(); i++) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i) || clicado == i) {
                    partida.escolherUpgrade(i);
                    jogo.sons().tocar("clique", 0.8f);
                    break;
                }
            }
        }
    }

    private void reagir(List<Evento> eventos) {
        Sons s = jogo.sons();
        for (Evento e : eventos) {
            switch (e) {
                case TIRO -> s.tocar("tiro", 0.18f, 0.09f);
                case INIMIGO_DERROTADO -> s.tocar("explosao", 0.35f, 0.06f);
                case XP_COLETADO -> s.tocar("xp", 0.22f, 0.05f);
                case SUBIU_NIVEL -> s.tocar("nivel", 0.7f);
                case NAVE_ATINGIDA -> {
                    tremor = Math.min(1f, tremor + 0.5f);
                    s.tocar("dano", 0.6f, 0.15f);
                }
                case PASSAGEIRO_RESGATADO -> {
                    Vetor p = partida.nave().posicao();
                    particulas.explosao(p.x(), -p.y(), 6, 16, 6f);
                    s.tocar("resgate", 0.8f);
                }
                case PASSAGEIRO_APARECEU -> s.tocar("clique", 0.8f);
                case PLATAFORMA_ABERTA -> s.tocar("plataforma", 0.8f);
                case CHEFE_APARECEU -> {
                    tremor = 1f;
                    s.tocar("chefe", 0.9f);
                }
                case ONDA_DE_CHOQUE -> s.tocar("dano", 0.25f, 0.3f);
                default -> { }
            }
            hud.notificar(e);
        }
    }

    // ---------- desenho ----------

    private void desenharPbr(ModelBatch batch, Environment env) {
        batch.render(mundo.kenney.plataforma.em(0, 0, 0, 0, 0, 1f), env);
        for (Passageiro p : partida.passageirosNoMapa()) {
            Modelo3D m = p instanceof Professor ? mundo.kenney.professor
                    : p instanceof Engenheiro ? mundo.kenney.engenheiro : mundo.kenney.astronauta;
            float pulo = m == mundo.kenney.astronauta ? Math.abs(MathUtils.sin(relogio * 3f + p.posicao().x())) * 0.25f : 0f;
            batch.render(m.em(p.nome().hashCode(), p.posicao().x(), pulo, -p.posicao().y(),
                    angulo(partida.nave().posicao().menos(p.posicao())), 1f), env);
        }
        Vetor nave = partida.nave().posicao();
        for (Inimigo i : partida.inimigos()) {
            Modelo3D m = mundo.kenney.inimigos[i.tipo().ordinal()];
            float[] info = rastroInimigos.get(i);
            float pulso = info != null && info[3] > 0 ? 1.15f : 1f;
            int variante = varianteInimigo.computeIfAbsent(i, k -> proximaVariante++);
            batch.render(m.em(variante, i.posicao().x(), 0, -i.posicao().y(), angulo(nave.menos(i.posicao())), pulso), env);
        }
        if (partida.estado() != EstadoPartida.DERROTA) {
            float flutuar = 0.8f + MathUtils.sin(relogio * 3f) * 0.12f;
            ModelInstance n = mundo.kenney.nave.em(0, nave.x(), flutuar, -nave.y(), angulo(partida.nave().direcao()), 1f);
            n.transform.rotate(Vector3.X, naveAndando ? -6f : 0f);
            batch.render(n, env);
        }
    }

    private void desenharEfeitos(ModelBatch batch, Environment env) {
        if (partida.plataformaAberta()) {
            float pulso = 1f + 0.06f * MathUtils.sin(relogio * 5f);
            iAnelAberto.transform.setToTranslation(0, 0.3f, 0).scale(pulso * 1.4f, 1f, pulso * 1.4f);
            batch.render(iAnelAberto, env);
            iFeixePlataforma.transform.setToTranslation(0, 0, 0).rotate(Vector3.Y, relogio * 30f);
            batch.render(iFeixePlataforma, env);
        }
        for (Passageiro p : partida.passageirosNoMapa()) {
            ModelInstance f = p instanceof Professor ? iFeixeProfessor : p instanceof Engenheiro ? iFeixeEngenheiro : iFeixeAstronauta;
            f.transform.setToTranslation(p.posicao().x(), 0, -p.posicao().y());
            batch.render(f, env);
        }
        for (CristalXp c : partida.cristais()) {
            float s = c.valor() >= 5 ? 1.6f : 1f;
            iCristal.transform.setToTranslation(c.posicao().x(), 0.5f + MathUtils.sin(relogio * 4f + c.posicao().y()) * 0.15f,
                    -c.posicao().y()).rotate(Vector3.Y, relogio * 120f).scale(s, s, s);
            batch.render(iCristal, env);
        }
        for (Projetil p : partida.projeteis()) {
            iProjetil.transform.setToTranslation(p.posicao().x(), 0.8f, -p.posicao().y()).rotate(Vector3.Y, angulo(p.velocidade()));
            batch.render(iProjetil, env);
        }
        Vetor pos = partida.nave().posicao();
        partida.arma(Orbital.class).ifPresent(o -> {
            for (Vetor e : o.posicoes(pos)) {
                iOrbital.transform.setToTranslation(e.x(), 0.8f, -e.y()).rotate(Vector3.Y, relogio * 200f);
                batch.render(iOrbital, env);
            }
        });
        partida.arma(OndaDeChoque.class).ifPresent(o -> {
            float t = o.tempoDesdeUltimaOnda();
            if (t < 0.45f) {
                float progresso = t / 0.45f;
                float r = o.raio() * progresso;
                BlendingAttribute b = (BlendingAttribute) iOnda.materials.get(0).get(BlendingAttribute.Type);
                b.opacity = 0.6f * (1 - progresso);
                iOnda.transform.setToTranslation(pos.x(), 0.3f, -pos.y()).scale(r, 1f, r);
                batch.render(iOnda, env);
            }
        });
        Vetor alvo = partida.plataformaAberta() ? Vetor.ZERO
                : partida.passageiroMaisProximo().map(Passageiro::posicao).orElse(null);
        if (alvo != null && alvo.distancia(pos) > 4f) {
            Vetor d = alvo.menos(pos).normalizado();
            iSeta.transform.setToTranslation(pos.x() + d.x() * 2.4f, 0.15f, -(pos.y() + d.y() * 2.4f)).rotate(Vector3.Y, angulo(d));
            batch.render(iSeta, env);
        }
        particulas.desenhar(batch, env);
        particulas.desenharPoeira(batch, env, alvoCamera.x, alvoCamera.z, relogio);
    }

    /** Ângulo (graus, em torno de Y) para um modelo com frente em +Z olhar na direção do domínio. */
    private static float angulo(Vetor direcao) {
        return MathUtils.atan2(direcao.x(), -direcao.y()) * MathUtils.radiansToDegrees;
    }

    private void atualizarLuzes() {
        Vetor p = partida.nave().posicao();
        Vetor d = partida.nave().direcao();
        mundo.luzMotor.position.set(p.x() - d.x() * 1.4f, 1.1f, -(p.y() - d.y() * 1.4f));
        mundo.luzMotor.intensity = partida.estado() == EstadoPartida.DERROTA ? 0f : 9f + MathUtils.sin(relogio * 30f) * 2f;
        mundo.luzOrbital.intensity = 0f;
        partida.arma(Orbital.class).ifPresent(o -> {
            Vetor e = o.posicoes(p).get(0);
            mundo.luzOrbital.position.set(e.x(), 1f, -e.y());
            mundo.luzOrbital.intensity = 7f;
        });
        if (partida.plataformaAberta()) {
            mundo.luzPlataforma.color.set(0.2f, 1f, 0.35f, 1f);
            mundo.luzPlataforma.intensity = 30f + MathUtils.sin(relogio * 5f) * 8f;
        } else {
            mundo.luzPlataforma.color.set(1f, 0.55f, 0.15f, 1f);
            mundo.luzPlataforma.intensity = 6f;
        }
    }

    /** Compara com o quadro anterior: vida caiu = pulso + número de dano; sumiu = explosão. */
    private void acompanharInimigos(float delta) {
        Map<Inimigo, float[]> vivos = new IdentityHashMap<>();
        for (Inimigo i : partida.inimigos()) {
            float[] antes = rastroInimigos.get(i);
            float pulso = antes == null ? 0 : Math.max(0, antes[3] - delta);
            float acumulado = antes == null ? 0 : antes[4] + Math.max(0, antes[2] - i.vida());
            float relogioDano = antes == null ? 0 : antes[5] + delta;
            if (antes != null && i.vida() < antes[2] - 0.01f) {
                pulso = 0.1f;
            }
            if (acumulado >= 1f && relogioDano > 0.18f) {
                hud.numeroDano(i.posicao().x(), -i.posicao().y(), (int) acumulado, i.tipo() == TipoInimigo.CHEFE);
                acumulado = 0;
                relogioDano = 0;
            }
            vivos.put(i, new float[] {i.posicao().x(), i.posicao().y(), i.vida(), pulso, acumulado, relogioDano});
        }
        for (Map.Entry<Inimigo, float[]> e : rastroInimigos.entrySet()) {
            if (!vivos.containsKey(e.getKey())) {
                float[] d = e.getValue();
                int tipo = e.getKey().tipo().ordinal();
                boolean chefe = tipo == 3;
                particulas.explosao(d[0], -d[1], tipo, chefe ? 70 : tipo == 2 ? 24 : 12, chefe ? 15f : 7f);
                varianteInimigo.remove(e.getKey());
                if (chefe) {
                    tremor = 1f;
                    jogo.sons().tocar("explosao_grande", 1f);
                }
            }
        }
        rastroInimigos.clear();
        rastroInimigos.putAll(vivos);
        if (partida.estado() == EstadoPartida.DERROTA && tempoDesdeFim < 0.05f) {
            Vetor n = partida.nave().posicao();
            particulas.explosao(n.x(), -n.y(), 4, 60, 12f);
            jogo.sons().tocar("explosao_grande", 1f);
        }
    }

    private void atualizarRastroMotor(float delta) {
        if (partida.estado() == EstadoPartida.DERROTA) {
            return;
        }
        tempoRastroMotor -= delta;
        if (tempoRastroMotor <= 0) {
            Vetor p = partida.nave().posicao();
            Vetor d = partida.nave().direcao();
            particulas.rastro(p.x() - d.x() * 1.3f, 0.9f, -(p.y() - d.y() * 1.3f));
            tempoRastroMotor = 0.03f;
        }
    }

    private void posicionarCamera(float delta) {
        Vetor p = partida.nave().posicao();
        alvoCamera.lerp(new Vector3(p.x(), 0, -p.y()), Math.min(1f, delta * 6f));
        float tx = tremor > 0 ? MathUtils.random(-1f, 1f) * tremor * 0.35f : 0;
        float tz = tremor > 0 ? MathUtils.random(-1f, 1f) * tremor * 0.35f : 0;
        mundo.camera.position.set(alvoCamera).add(DESLOCAMENTO_CAMERA).add(tx, 0, tz);
        mundo.camera.up.set(Vector3.Y);
        mundo.camera.lookAt(alvoCamera.x + tx, 0, alvoCamera.z + tz);
        mundo.camera.update();
    }

    // ---------- modo demonstração (CI) ----------

    private Vetor pilotoAutomatico() {
        if (partida.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
            partida.escolherUpgrade(0);
        }
        Vetor pos = partida.nave().posicao();
        Vetor alvo = partida.passageiroMaisProximo().map(Passageiro::posicao).orElse(Vetor.polar(relogio * 0.5f, 8f));
        Vetor dir = alvo.menos(pos).normalizado();
        for (Inimigo i : partida.inimigos()) {
            float d = i.posicao().distancia(pos);
            if (d < 4f) {
                dir = dir.mais(pos.menos(i.posicao()).normalizado().vezes((4f - d) / 2f));
            }
        }
        return dir;
    }

    private void capturarDemonstracao() {
        float[] instantes = {4f, 15f, 30f, 45f};
        if (capturas < instantes.length && relogio >= instantes[capturas]) {
            jogo.salvarCaptura("captura-" + capturas + ".png");
            capturas++;
        }
        if (relogio > 47f) {
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {
        mundo.redimensionar(width, height);
    }

    @Override
    public void pause() {
        pausado = true;
    }
}
