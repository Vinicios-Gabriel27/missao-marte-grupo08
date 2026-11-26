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
import br.edu.unifor.missaomarte.sobrevivencia.Vetor;
import br.edu.unifor.missaomarte.sobrevivencia.armas.OndaDeChoque;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Orbital;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Tela 3D da partida. Lê o controle, chama {@link PartidaSobrevivencia#atualizar} e desenha
 * o estado. Nenhuma regra de jogo fica aqui: só tradução de entrada e desenho.
 *
 * Mundo: domínio (x, y) → 3D (x, 0, -y). Assim "cima" no controle afasta da câmera.
 */
@SuppressWarnings("deprecation")
public class TelaJogo extends ScreenAdapter {
    private static final Color CEU = new Color(0.55f, 0.3f, 0.22f, 1f);
    private static final Vector3 DESLOCAMENTO_CAMERA = new Vector3(0, 16f, 11f);

    private final MissaoMarteJogo jogo;
    private final PartidaSobrevivencia partida;
    private final Modelos modelos = new Modelos();
    private final ModelBatch modelBatch = new ModelBatch();
    private final ModelBatch sombraBatch = new ModelBatch(new DepthShaderProvider());
    private final DirectionalShadowLight luzSombra;
    private final Particulas particulas;
    /** Último estado conhecido de cada inimigo, para detectar acerto (pulso) e morte (explosão). */
    private final Map<Inimigo, float[]> rastroInimigos = new IdentityHashMap<>();
    private float tempoRastroMotor;
    private final Environment ambiente = new Environment();
    private final PerspectiveCamera camera;
    private final Cenario cenario;
    private final Hud hud;

    // Uma instância por modelo, reposicionada para cada entidade (o ModelBatch copia a transformação).
    private final ModelInstance iNave;
    private final ModelInstance iProjetil;
    private final ModelInstance iCristal;
    private final ModelInstance iProfessor;
    private final ModelInstance iEngenheiro;
    private final ModelInstance iAstronauta;
    private final ModelInstance iPlataforma;
    private final ModelInstance iPlataformaAberta;
    private final ModelInstance iOrbital;
    private final ModelInstance iOnda;
    private final ModelInstance iSeta;
    private final ModelInstance[] iInimigos;

    private final Vector3 alvoCamera = new Vector3();
    private float tremor;
    private float relogio;
    private float tempoDesdeFim;
    private boolean pausado;

    public TelaJogo(MissaoMarteJogo jogo, Dificuldade dificuldade) {
        this.jogo = jogo;
        long semente = System.nanoTime();
        this.partida = new PartidaSobrevivencia(dificuldade, new Random(semente));
        this.cenario = new Cenario(modelos, partida.arena(), semente);
        this.hud = new Hud(jogo, partida);

        ambiente.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.42f, 0.34f, 0.34f, 1f));
        ambiente.set(new ColorAttribute(ColorAttribute.Fog, CEU));
        luzSombra = new DirectionalShadowLight(2048, 2048, 46f, 46f, 1f, 100f);
        luzSombra.set(1f, 0.9f, 0.78f, -0.45f, -1f, -0.35f);
        ambiente.add(luzSombra);
        ambiente.shadowMap = luzSombra;
        ambiente.add(new DirectionalLight().set(0.18f, 0.12f, 0.2f, 0.5f, -0.4f, 0.6f)); // luz de preenchimento
        particulas = new Particulas(modelos);

        camera = new PerspectiveCamera(55, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.5f;
        camera.far = 90f;

        iNave = new ModelInstance(modelos.nave);
        iProjetil = new ModelInstance(modelos.projetil);
        iCristal = new ModelInstance(modelos.cristal);
        iProfessor = new ModelInstance(modelos.professor);
        iEngenheiro = new ModelInstance(modelos.engenheiro);
        iAstronauta = new ModelInstance(modelos.astronauta);
        iPlataforma = new ModelInstance(modelos.plataforma);
        iPlataformaAberta = new ModelInstance(modelos.plataformaAberta);
        iOrbital = new ModelInstance(modelos.orbital);
        iOnda = new ModelInstance(modelos.onda);
        iSeta = new ModelInstance(modelos.seta);
        iInimigos = new ModelInstance[] {
            new ModelInstance(modelos.perseguidor), new ModelInstance(modelos.rapido),
            new ModelInstance(modelos.tanque), new ModelInstance(modelos.chefe)
        };
        posicionarCamera(1f);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 20f);
        relogio += delta;
        lerEntradaGeral();
        if (!pausado) {
            List<Evento> eventos = partida.atualizar(delta, ControleTeclado.direcao());
            reagir(eventos);
        }
        if (partida.estado().encerrada()) {
            tempoDesdeFim += delta;
            if (tempoDesdeFim > 1.8f) {
                jogo.trocarTela(new TelaFim(jogo, partida));
                return;
            }
        }
        tremor = Math.max(0, tremor - delta * 2.5f);
        posicionarCamera(delta);
        if (!pausado) {
            acompanharInimigos(delta);
            atualizarRastroMotor(delta);
            particulas.atualizar(delta);
        }

        // 1ª passada: profundidade vista da luz (mapa de sombras)
        luzSombra.begin(alvoCamera, luzSombra.direction);
        sombraBatch.begin(luzSombra.getCamera());
        desenharMundo(sombraBatch, null, false);
        sombraBatch.end();
        luzSombra.end();

        // 2ª passada: cena normal, já com sombras e névoa
        ScreenUtils.clear(CEU, true);
        modelBatch.begin(camera);
        modelBatch.render(cenario.instancias, ambiente);
        desenharMundo(modelBatch, ambiente, true);
        particulas.desenhar(modelBatch, ambiente);
        modelBatch.end();

        hud.desenhar(pausado);
    }

    private void lerEntradaGeral() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pausado = !pausado;
        }
        if (pausado && Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            jogo.trocarTela(new TelaMenu(jogo));
            return;
        }
        if (partida.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
            int opcoes = partida.opcoesUpgrade().size();
            for (int i = 0; i < opcoes; i++) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i) || hud.cartaoClicado() == i) {
                    partida.escolherUpgrade(i);
                    break;
                }
            }
        }
    }

    /** Efeitos visuais para eventos do domínio (som entra na Fase 4). */
    private void reagir(List<Evento> eventos) {
        for (Evento e : eventos) {
            switch (e) {
                case NAVE_ATINGIDA -> tremor = Math.min(1f, tremor + 0.5f);
                case CHEFE_APARECEU -> tremor = 1f;
                default -> { }
            }
            hud.notificar(e);
        }
    }

    private void desenharMundo(ModelBatch batch, Environment env, boolean efeitos) {
        if (!efeitos) {
            batch.render(cenario.instancias);
        }
        desenharPlataforma(batch, env);
        desenharPassageiros(batch, env);
        desenharCristais(batch, env);
        desenharInimigos(batch, env);
        desenharProjeteis(batch, env);
        desenharNaveEArmas(batch, env, efeitos);
    }

    /** Compara com o quadro anterior: vida caiu = pulso; sumiu da lista = explosão. */
    private void acompanharInimigos(float delta) {
        Map<Inimigo, float[]> vivos = new IdentityHashMap<>();
        for (Inimigo i : partida.inimigos()) {
            float[] antes = rastroInimigos.get(i);
            float pulso = antes == null ? 0 : Math.max(0, antes[3] - delta);
            if (antes != null && i.vida() < antes[2] - 0.01f) {
                pulso = 0.12f;
            }
            vivos.put(i, new float[] {i.posicao().x(), i.posicao().y(), i.vida(), pulso});
        }
        for (Map.Entry<Inimigo, float[]> e : rastroInimigos.entrySet()) {
            if (!vivos.containsKey(e.getKey())) {
                float[] d = e.getValue();
                int tipo = e.getKey().tipo().ordinal();
                boolean chefe = tipo == 3;
                particulas.explosao(d[0], -d[1], tipo, chefe ? 60 : tipo == 2 ? 22 : 12, chefe ? 14f : 7f);
                if (chefe) {
                    tremor = 1f;
                }
            }
        }
        rastroInimigos.clear();
        rastroInimigos.putAll(vivos);
    }

    private void atualizarRastroMotor(float delta) {
        tempoRastroMotor -= delta;
        if (tempoRastroMotor <= 0) {
            Vetor p = partida.nave().posicao();
            Vetor d = partida.nave().direcao();
            particulas.rastro(p.x() - d.x() * 1.1f, 0.7f, -(p.y() - d.y() * 1.1f));
            tempoRastroMotor = 0.03f;
        }
    }

    private void posicionarCamera(float delta) {
        Vetor p = partida.nave().posicao();
        alvoCamera.lerp(new Vector3(p.x(), 0, -p.y()), Math.min(1f, delta * 6f));
        float tx = tremor > 0 ? MathUtils.random(-1f, 1f) * tremor * 0.35f : 0;
        float tz = tremor > 0 ? MathUtils.random(-1f, 1f) * tremor * 0.35f : 0;
        camera.position.set(alvoCamera).add(DESLOCAMENTO_CAMERA).add(tx, 0, tz);
        camera.up.set(Vector3.Y);
        camera.lookAt(alvoCamera.x + tx, 0, alvoCamera.z + tz);
        camera.update();
    }

    private void desenharPlataforma(ModelBatch batch, Environment env) {
        ModelInstance inst = partida.plataformaAberta() ? iPlataformaAberta : iPlataforma;
        inst.transform.setToTranslation(0, 0, 0).rotate(Vector3.Y, relogio * 20f);
        batch.render(inst, env);
    }

    private void desenharPassageiros(ModelBatch batch, Environment env) {
        for (Passageiro p : partida.passageirosNoMapa()) {
            ModelInstance inst = p instanceof Professor ? iProfessor : p instanceof Engenheiro ? iEngenheiro : iAstronauta;
            float pulo = Math.abs(MathUtils.sin(relogio * 3f + p.posicao().x())) * 0.3f;
            inst.transform.setToTranslation(p.posicao().x(), pulo, -p.posicao().y()).rotate(Vector3.Y, relogio * 60f);
            batch.render(inst, env);
        }
    }

    private void desenharCristais(ModelBatch batch, Environment env) {
        for (CristalXp c : partida.cristais()) {
            float s = c.valor() >= 5 ? 1.6f : 1f;
            iCristal.transform.setToTranslation(c.posicao().x(), 0.5f + MathUtils.sin(relogio * 4f + c.posicao().y()) * 0.15f,
                    -c.posicao().y()).rotate(Vector3.Y, relogio * 120f).scale(s, s, s);
            batch.render(iCristal, env);
        }
    }

    private void desenharInimigos(ModelBatch batch, Environment env) {
        Vetor nave = partida.nave().posicao();
        for (Inimigo i : partida.inimigos()) {
            ModelInstance inst = iInimigos[i.tipo().ordinal()];
            Vetor dir = nave.menos(i.posicao());
            float yaw = MathUtils.atan2(dir.x(), -dir.y()) * MathUtils.radiansToDegrees;
            float balanco = MathUtils.sin(relogio * 10f + i.posicao().x() * 3f) * 0.08f;
            float[] info = rastroInimigos.get(i);
            float escala = info != null && info[3] > 0 ? 1.18f : 1f;
            inst.transform.setToTranslation(i.posicao().x(), Math.abs(balanco), -i.posicao().y()).rotate(Vector3.Y, yaw)
                    .scale(escala, escala, escala);
            batch.render(inst, env);
        }
    }

    private void desenharProjeteis(ModelBatch batch, Environment env) {
        for (Projetil p : partida.projeteis()) {
            float yaw = MathUtils.atan2(p.velocidade().x(), -p.velocidade().y()) * MathUtils.radiansToDegrees;
            iProjetil.transform.setToTranslation(p.posicao().x(), 0.6f, -p.posicao().y()).rotate(Vector3.Y, yaw);
            batch.render(iProjetil, env);
        }
    }

    private void desenharNaveEArmas(ModelBatch batch, Environment env, boolean efeitos) {
        Vetor pos = partida.nave().posicao();
        Vetor dir = partida.nave().direcao();
        float yaw = MathUtils.atan2(dir.x(), -dir.y()) * MathUtils.radiansToDegrees;
        float flutuar = 0.7f + MathUtils.sin(relogio * 3f) * 0.12f;
        iNave.transform.setToTranslation(pos.x(), flutuar, -pos.y()).rotate(Vector3.Y, yaw);
        batch.render(iNave, env);

        partida.arma(Orbital.class).ifPresent(o -> {
            for (Vetor e : o.posicoes(pos)) {
                iOrbital.transform.setToTranslation(e.x(), 0.7f, -e.y()).rotate(Vector3.Y, relogio * 200f);
                batch.render(iOrbital, env);
            }
        });

        if (!efeitos) {
            return;
        }
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
            float yawSeta = MathUtils.atan2(d.x(), -d.y()) * MathUtils.radiansToDegrees;
            iSeta.transform.setToTranslation(pos.x() + d.x() * 2.2f, 0.15f, -(pos.y() + d.y() * 2.2f)).rotate(Vector3.Y, yawSeta);
            batch.render(iSeta, env);
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {
        pausado = true;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        sombraBatch.dispose();
        luzSombra.dispose();
        modelos.dispose();
    }
}
