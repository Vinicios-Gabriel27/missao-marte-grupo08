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
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

/**
 * Tela 3D da partida. Lê o controle, chama {@link PartidaSobrevivencia#atualizar} e desenha
 * o estado. Nenhuma regra de jogo fica aqui: só tradução de entrada e desenho.
 *
 * Duas renderizações: PBR (gdx-gltf) para os modelos Kenney e a clássica da libGDX para
 * chão, efeitos e brilhos. As duas usam o mesmo mapa de sombras.
 * Mundo: domínio (x, y) → 3D (x, 0, -y). Assim "cima" no controle afasta da câmera.
 */
public class TelaJogo extends ScreenAdapter {
    private static final Color CEU = new Color(0.62f, 0.36f, 0.26f, 1f);
    private static final Vector3 DESLOCAMENTO_CAMERA = new Vector3(0, 12.5f, 11.5f);
    private static final String[] ANIM_INIMIGO = {"walk", "sprint", "walk", "walk"};

    private final MissaoMarteJogo jogo;
    private final PartidaSobrevivencia partida;
    private final Modelos modelos = new Modelos();
    private final ModelosKenney kenney = new ModelosKenney();
    private final ModelBatch pbrBatch;
    private final ModelBatch classicoBatch = new ModelBatch();
    private final ModelBatch sombraBatch;
    private final Environment ambientePbr = new Environment();
    private final Environment ambienteClassico = new Environment();
    private final DirectionalShadowLight sol;
    private final Cubemap mapaDifuso;
    private final Cubemap mapaEspecular;
    private final Texture brdf;
    private final PerspectiveCamera camera;
    private final Cenario cenario;
    private final Hud hud;
    private final Particulas particulas;
    private final PosProcessamento pos = new PosProcessamento();
    private final PointLightEx luzMotor = new PointLightEx();
    private final PointLightEx luzOrbital = new PointLightEx();
    private final PointLightEx luzPlataforma = new PointLightEx();

    private final ModelInstance iProjetil;
    private final ModelInstance iCristal;
    private final ModelInstance iOrbital;
    private final ModelInstance iOnda;
    private final ModelInstance iSeta;
    private final ModelInstance iFeixeProfessor;
    private final ModelInstance iFeixeEngenheiro;
    private final ModelInstance iFeixeAstronauta;
    private final ModelInstance iFeixePlataforma;
    private final ModelInstance iAnel;
    private final ModelInstance iAnelAberto;

    /** Último estado de cada inimigo: x, y, vida, pulso. Detecta acerto (pulso) e morte (explosão). */
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
    private final boolean demo = MissaoMarteJogo.modoDemonstracao();
    private int capturas;

    public TelaJogo(MissaoMarteJogo jogo, Dificuldade dificuldade) {
        this.jogo = jogo;
        long semente = System.nanoTime();
        this.partida = new PartidaSobrevivencia(dificuldade, new Random(semente));
        this.cenario = new Cenario(modelos, kenney, partida.arena(), semente);
        this.hud = new Hud(jogo, partida);
        this.particulas = new Particulas(modelos);

        PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
        config.numBones = 16;
        config.numDirectionalLights = 2;
        config.numPointLights = 3;
        pbrBatch = new ModelBatch(PBRShaderProvider.createDefault(config));
        sombraBatch = new ModelBatch(PBRShaderProvider.createDefaultDepth(16));

        // Sol de fim de tarde marciano, com sombras
        sol = new DirectionalShadowLight(2048, 2048, 48f, 48f, 1f, 120f);
        sol.direction.set(-0.45f, -1f, -0.35f).nor();
        sol.baseColor.set(1f, 0.88f, 0.75f, 1f);
        sol.intensity = 3.6f;
        sol.updateColor();

        // Luz de ambiente (IBL) com céu e chão em tons de Marte
        IBLBuilder ibl = IBLBuilder.createOutdoor(sol);
        ibl.nearGroundColor.set(0.3f, 0.14f, 0.09f, 1f);
        ibl.farGroundColor.set(0.2f, 0.09f, 0.06f, 1f);
        ibl.nearSkyColor.set(0.55f, 0.38f, 0.32f, 1f);
        ibl.farSkyColor.set(0.35f, 0.25f, 0.28f, 1f);
        mapaDifuso = ibl.buildIrradianceMap(256);
        mapaEspecular = ibl.buildRadianceMap(10);
        ibl.dispose();
        brdf = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        ambientePbr.add(sol);
        luzMotor.color.set(1f, 0.55f, 0.2f, 1f);
        luzMotor.intensity = 18f;
        luzMotor.range = 9f;
        luzOrbital.color.set(0.75f, 0.4f, 1f, 1f);
        luzOrbital.intensity = 0f;
        luzOrbital.range = 7f;
        luzPlataforma.color.set(1f, 0.55f, 0.15f, 1f);
        luzPlataforma.intensity = 14f;
        luzPlataforma.range = 8f;
        luzPlataforma.position.set(0, 1.5f, 0);
        ambientePbr.add(luzMotor);
        ambientePbr.add(luzOrbital);
        ambientePbr.add(luzPlataforma);
        ambientePbr.shadowMap = sol;
        ambientePbr.set(PBRCubemapAttribute.createDiffuseEnv(mapaDifuso));
        ambientePbr.set(PBRCubemapAttribute.createSpecularEnv(mapaEspecular));
        ambientePbr.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdf));
        ambientePbr.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 1f / 512f));
        ambientePbr.set(new ColorAttribute(ColorAttribute.Fog, CEU));
        ambientePbr.set(FogAttribute.createFog(32f, 95f, 1.5f));

        ambienteClassico.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.32f, 0.24f, 0.24f, 1f));
        ambienteClassico.set(new ColorAttribute(ColorAttribute.Fog, CEU));
        ambienteClassico.add(new DirectionalLight().set(1f, 0.88f, 0.75f, sol.direction));
        ambienteClassico.shadowMap = sol;

        camera = new PerspectiveCamera(52, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.5f;
        camera.far = 90f;

        iProjetil = new ModelInstance(modelos.projetil);
        iCristal = new ModelInstance(modelos.cristal);
        iOrbital = new ModelInstance(modelos.orbital);
        iOnda = new ModelInstance(modelos.onda);
        iSeta = new ModelInstance(modelos.seta);
        iFeixeProfessor = new ModelInstance(modelos.feixeProfessor);
        iFeixeEngenheiro = new ModelInstance(modelos.feixeEngenheiro);
        iFeixeAstronauta = new ModelInstance(modelos.feixeAstronauta);
        iFeixePlataforma = new ModelInstance(modelos.feixePlataforma);
        iAnel = new ModelInstance(modelos.anelPlataforma);
        iAnelAberto = new ModelInstance(modelos.anelPlataformaAberta);
        for (int i = 0; i < ANIM_INIMIGO.length; i++) {
            kenney.inimigos[i].trocarAnimacao(ANIM_INIMIGO[i]);
        }
        posicionarCamera(1f);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 20f);
        relogio += delta;
        lerEntradaGeral();
        if (!pausado) {
            Vetor entrada = demo ? pilotoAutomatico() : ControleTeclado.direcao();
            naveAndando = entrada.comprimento() > 0.01f;
            List<Evento> eventos = partida.atualizar(delta, entrada);
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
        if (!pausado && partida.estado() == EstadoPartida.EM_ANDAMENTO) {
            kenney.atualizar(delta);
            acompanharInimigos(delta);
            atualizarRastroMotor(delta);
            particulas.atualizar(delta);
        }

        // 1ª passada: profundidade vista do sol (mapa de sombras)
        sol.setCenter(alvoCamera);
        sol.begin();
        sombraBatch.begin(sol.getCamera());
        sombraBatch.render(cenario.instanciasPbr);
        desenharPbr(sombraBatch, null);
        sombraBatch.end();
        sol.end();

        atualizarLuzes();

        // 2ª passada: cena com sombras, névoa e luz de ambiente (desenhada numa textura)
        pos.comecar();
        ScreenUtils.clear(CEU, true);
        pbrBatch.begin(camera);
        pbrBatch.render(cenario.instanciasPbr, ambientePbr);
        desenharPbr(pbrBatch, ambientePbr);
        pbrBatch.end();

        classicoBatch.begin(camera);
        desenharEfeitos(classicoBatch, ambienteClassico);
        particulas.desenhar(classicoBatch, ambienteClassico);
        particulas.desenharPoeira(classicoBatch, ambienteClassico, alvoCamera.x, alvoCamera.z, relogio);
        classicoBatch.end();
        pos.terminar(); // bloom, cor e vinheta

        hud.desenhar(pausado);
        if (demo) {
            capturarDemonstracao();
        }
    }

    /** Piloto automático do modo demonstração: busca passageiros e desvia dos inimigos. */
    private Vetor pilotoAutomatico() {
        if (partida.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
            partida.escolherUpgrade(0);
        }
        Vetor pos = partida.nave().posicao();
        Vetor alvo = partida.passageiroMaisProximo().map(Passageiro::posicao)
                .orElse(Vetor.polar(relogio * 0.5f, 8f));
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
            com.badlogic.gdx.graphics.Pixmap p = com.badlogic.gdx.utils.ScreenUtils.getFrameBufferPixmap(
                    0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            com.badlogic.gdx.graphics.PixmapIO.writePNG(Gdx.files.local("captura-" + capturas + ".png"), p, 6, true);
            p.dispose();
            Gdx.app.log("DEMO", "captura " + capturas + " salva");
            capturas++;
        }
        if (relogio > 47f) {
            Gdx.app.exit();
        }
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
            int clicado = hud.cartaoClicado();
            for (int i = 0; i < partida.opcoesUpgrade().size(); i++) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i) || clicado == i) {
                    partida.escolherUpgrade(i);
                    break;
                }
            }
        }
    }

    /** Luzes que acompanham a nave, o orbital e a plataforma (iluminam chão e inimigos). */
    private void atualizarLuzes() {
        Vetor p = partida.nave().posicao();
        Vetor d = partida.nave().direcao();
        luzMotor.position.set(p.x() - d.x() * 1.4f, 1.1f, -(p.y() - d.y() * 1.4f));
        luzMotor.intensity = 14f + MathUtils.sin(relogio * 30f) * 3f;
        luzOrbital.intensity = 0f;
        partida.arma(Orbital.class).ifPresent(o -> {
            Vetor e = o.posicoes(p).get(0);
            luzOrbital.position.set(e.x(), 1f, -e.y());
            luzOrbital.intensity = 10f;
        });
        if (partida.plataformaAberta()) {
            luzPlataforma.color.set(0.2f, 1f, 0.35f, 1f);
            luzPlataforma.intensity = 30f + MathUtils.sin(relogio * 5f) * 8f;
        } else {
            luzPlataforma.intensity = 10f;
        }
    }

    private void reagir(List<Evento> eventos) {
        for (Evento e : eventos) {
            switch (e) {
                case NAVE_ATINGIDA -> tremor = Math.min(1f, tremor + 0.5f);
                case CHEFE_APARECEU -> tremor = 1f;
                case PASSAGEIRO_RESGATADO -> {
                    Vetor p = partida.nave().posicao();
                    particulas.explosao(p.x(), -p.y(), 6, 16, 6f);
                }
                default -> { }
            }
            hud.notificar(e);
        }
    }

    // ---------- modelos Kenney (PBR) ----------

    private void desenharPbr(ModelBatch batch, Environment env) {
        Modelo3D plat = kenney.plataforma;
        batch.render(plat.em(0, 0, 0, 0, 0, 1f), env);

        for (Passageiro p : partida.passageirosNoMapa()) {
            Modelo3D m = p instanceof Professor ? kenney.professor : p instanceof Engenheiro ? kenney.engenheiro : kenney.astronauta;
            float pulo = m == kenney.astronauta ? Math.abs(MathUtils.sin(relogio * 3f + p.posicao().x())) * 0.25f : 0f;
            float olharNave = angulo(partida.nave().posicao().menos(p.posicao()));
            batch.render(m.em(p.nome().hashCode(), p.posicao().x(), pulo, -p.posicao().y(), olharNave, 1f), env);
        }

        Vetor nave = partida.nave().posicao();
        for (Inimigo i : partida.inimigos()) {
            Modelo3D m = kenney.inimigos[i.tipo().ordinal()];
            float[] info = rastroInimigos.get(i);
            float pulso = info != null && info[3] > 0 ? 1.15f : 1f;
            int variante = varianteInimigo.computeIfAbsent(i, k -> proximaVariante++);
            batch.render(m.em(variante, i.posicao().x(), 0, -i.posicao().y(), angulo(nave.menos(i.posicao())), pulso), env);
        }

        Vetor dir = partida.nave().direcao();
        float flutuar = 0.8f + MathUtils.sin(relogio * 3f) * 0.12f;
        float inclinar = naveAndando ? 1f : 0f;
        ModelInstance n = kenney.nave.em(0, nave.x(), flutuar, -nave.y(), angulo(dir), 1f);
        n.transform.rotate(Vector3.X, -6f * inclinar);
        batch.render(n, env);
    }

    // ---------- efeitos (renderização clássica) ----------

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
    }

    /** Ângulo (graus, em torno de Y) para um modelo com frente em +Z olhar na direção do domínio. */
    private static float angulo(Vetor direcao) {
        return MathUtils.atan2(direcao.x(), -direcao.y()) * MathUtils.radiansToDegrees;
    }

    private void acompanharInimigos(float delta) {
        Map<Inimigo, float[]> vivos = new IdentityHashMap<>();
        for (Inimigo i : partida.inimigos()) {
            float[] antes = rastroInimigos.get(i);
            float pulso = antes == null ? 0 : Math.max(0, antes[3] - delta);
            if (antes != null && i.vida() < antes[2] - 0.01f) {
                pulso = 0.1f;
            }
            vivos.put(i, new float[] {i.posicao().x(), i.posicao().y(), i.vida(), pulso});
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
            particulas.rastro(p.x() - d.x() * 1.3f, 0.9f, -(p.y() - d.y() * 1.3f));
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

    @Override
    public void resize(int width, int height) {
        pos.redimensionar(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
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
        pos.dispose();
        pbrBatch.dispose();
        classicoBatch.dispose();
        sombraBatch.dispose();
        sol.dispose();
        mapaDifuso.dispose();
        mapaEspecular.dispose();
        brdf.dispose();
        kenney.dispose();
        modelos.dispose();
    }
}
