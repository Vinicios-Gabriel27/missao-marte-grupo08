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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.List;
import java.util.Random;

/**
 * Tela 3D da partida. Lê o controle, chama {@link PartidaSobrevivencia#atualizar} e desenha
 * o estado. Nenhuma regra de jogo fica aqui: só tradução de entrada e desenho.
 *
 * Mundo: domínio (x, y) → 3D (x, 0, -y). Assim "cima" no controle afasta da câmera.
 */
public class TelaJogo extends ScreenAdapter {
    private static final Color CEU = new Color(0.55f, 0.3f, 0.22f, 1f);
    private static final Vector3 DESLOCAMENTO_CAMERA = new Vector3(0, 16f, 11f);

    private final MissaoMarteJogo jogo;
    private final PartidaSobrevivencia partida;
    private final Modelos modelos = new Modelos();
    private final ModelBatch modelBatch = new ModelBatch();
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

        ambiente.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.42f, 0.4f, 1f));
        ambiente.add(new DirectionalLight().set(1f, 0.92f, 0.82f, -0.45f, -1f, -0.35f));

        camera = new PerspectiveCamera(55, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.5f;
        camera.far = 200f;

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

        ScreenUtils.clear(CEU, true);
        modelBatch.begin(camera);
        modelBatch.render(cenario.instancias, ambiente);
        desenharPlataforma();
        desenharPassageiros();
        desenharCristais();
        desenharInimigos();
        desenharProjeteis();
        desenharNaveEArmas();
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

    private void desenharPlataforma() {
        ModelInstance inst = partida.plataformaAberta() ? iPlataformaAberta : iPlataforma;
        inst.transform.setToTranslation(0, 0, 0).rotate(Vector3.Y, relogio * 20f);
        modelBatch.render(inst, ambiente);
    }

    private void desenharPassageiros() {
        for (Passageiro p : partida.passageirosNoMapa()) {
            ModelInstance inst = p instanceof Professor ? iProfessor : p instanceof Engenheiro ? iEngenheiro : iAstronauta;
            float pulo = Math.abs(MathUtils.sin(relogio * 3f + p.posicao().x())) * 0.3f;
            inst.transform.setToTranslation(p.posicao().x(), pulo, -p.posicao().y()).rotate(Vector3.Y, relogio * 60f);
            modelBatch.render(inst, ambiente);
        }
    }

    private void desenharCristais() {
        for (CristalXp c : partida.cristais()) {
            float s = c.valor() >= 5 ? 1.6f : 1f;
            iCristal.transform.setToTranslation(c.posicao().x(), 0.5f + MathUtils.sin(relogio * 4f + c.posicao().y()) * 0.15f,
                    -c.posicao().y()).rotate(Vector3.Y, relogio * 120f).scale(s, s, s);
            modelBatch.render(iCristal, ambiente);
        }
    }

    private void desenharInimigos() {
        Vetor nave = partida.nave().posicao();
        for (Inimigo i : partida.inimigos()) {
            ModelInstance inst = iInimigos[i.tipo().ordinal()];
            Vetor dir = nave.menos(i.posicao());
            float yaw = MathUtils.atan2(dir.x(), -dir.y()) * MathUtils.radiansToDegrees;
            float balanco = MathUtils.sin(relogio * 10f + i.posicao().x() * 3f) * 0.08f;
            inst.transform.setToTranslation(i.posicao().x(), Math.abs(balanco), -i.posicao().y()).rotate(Vector3.Y, yaw);
            modelBatch.render(inst, ambiente);
        }
    }

    private void desenharProjeteis() {
        for (Projetil p : partida.projeteis()) {
            iProjetil.transform.setToTranslation(p.posicao().x(), 0.6f, -p.posicao().y());
            modelBatch.render(iProjetil, ambiente);
        }
    }

    private void desenharNaveEArmas() {
        Vetor pos = partida.nave().posicao();
        Vetor dir = partida.nave().direcao();
        float yaw = MathUtils.atan2(dir.x(), -dir.y()) * MathUtils.radiansToDegrees;
        float flutuar = 0.7f + MathUtils.sin(relogio * 3f) * 0.12f;
        iNave.transform.setToTranslation(pos.x(), flutuar, -pos.y()).rotate(Vector3.Y, yaw);
        modelBatch.render(iNave, ambiente);

        partida.arma(Orbital.class).ifPresent(o -> {
            for (Vetor e : o.posicoes(pos)) {
                iOrbital.transform.setToTranslation(e.x(), 0.7f, -e.y()).rotate(Vector3.Y, relogio * 200f);
                modelBatch.render(iOrbital, ambiente);
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
                modelBatch.render(iOnda, ambiente);
            }
        });

        Vetor alvo = partida.plataformaAberta() ? Vetor.ZERO
                : partida.passageiroMaisProximo().map(Passageiro::posicao).orElse(null);
        if (alvo != null && alvo.distancia(pos) > 4f) {
            Vetor d = alvo.menos(pos).normalizado();
            float yawSeta = MathUtils.atan2(d.x(), -d.y()) * MathUtils.radiansToDegrees;
            iSeta.transform.setToTranslation(pos.x() + d.x() * 2.2f, 0.15f, -(pos.y() + d.y() * 2.2f)).rotate(Vector3.Y, yawSeta);
            modelBatch.render(iSeta, ambiente);
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
        modelos.dispose();
    }
}
