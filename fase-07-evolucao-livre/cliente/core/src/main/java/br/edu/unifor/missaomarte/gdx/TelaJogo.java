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
import br.edu.unifor.missaomarte.sobrevivencia.ComportamentoChefe;
import br.edu.unifor.missaomarte.sobrevivencia.Projetil;
import br.edu.unifor.missaomarte.sobrevivencia.ProjetilInimigo;
import br.edu.unifor.missaomarte.sobrevivencia.TipoInimigo;
import br.edu.unifor.missaomarte.sobrevivencia.Vetor;
import br.edu.unifor.missaomarte.sobrevivencia.armas.MisseisTeleguiados;
import br.edu.unifor.missaomarte.sobrevivencia.armas.OndaDeChoque;
import br.edu.unifor.missaomarte.sobrevivencia.armas.RaioEmCadeia;
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
    private final ModelInstance iProjetilInimigo;
    private final ModelInstance iAviso;
    private final ModelInstance iMissil;
    private final ModelInstance iRaio;
    private final ModelInstance iRaioNucleo;
    private final ModelInstance iRaioAura;
    private final ModelInstance iBrilhoFogo;
    private final ModelInstance iAnelFogo;
    /** Explosões de meteoro em andamento: x, z, tempo, raio final. */
    private final java.util.List<float[]> explosoesFogo = new java.util.ArrayList<>();
    private float flash;
    private float picoFlash;
    private final java.util.Set<MisseisTeleguiados.Missil> misseisVistos = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<MisseisTeleguiados.Missil, Vetor> ultimaPosMissil = new IdentityHashMap<>();

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
    private final ControleToque toque = new ControleToque();

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
        iProjetilInimigo = new ModelInstance(m.projetilInimigo);
        iAviso = new ModelInstance(m.aviso);
        iMissil = new ModelInstance(m.missil);
        iRaio = new ModelInstance(m.raio);
        iRaioNucleo = new ModelInstance(m.raioNucleo);
        iRaioAura = new ModelInstance(m.raioAura);
        iBrilhoFogo = new ModelInstance(m.brilhoFogo);
        iAnelFogo = new ModelInstance(m.anelFogo);
        mundo.luzPlataforma.color.set(1f, 0.55f, 0.15f, 1f);
        posicionarCamera(1f);
    }

    @Override
    public void show() {
        jogo.sons().tocarMusica();
        Gdx.input.setInputProcessor(toque);
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 20f);
        relogio += delta;
        jogo.sons().atualizar(delta);
        lerEntradaGeral();
        if (!pausado) {
            Vetor teclado = ControleTeclado.direcao();
            Vetor entrada = demo ? pilotoAutomatico() : teclado.comprimento() > 0 ? teclado : toque.direcao();
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
            for (int k = explosoesFogo.size() - 1; k >= 0; k--) {
                float[] e = explosoesFogo.get(k);
                e[2] += delta;
                if (e[2] > 0.5f) {
                    explosoesFogo.remove(k);
                }
            }
        }
        flash = Math.max(0, flash - delta * 4f);
        atualizarLuzes();
        mundo.desenhar(alvoCamera, cenario.instanciasPbr, this::desenharPbr, this::desenharEfeitos);
        hud.desenhar(pausado, delta, mundo.camera, toque);
        if (demo) {
            capturarDemonstracao();
        }
    }

    private void lerEntradaGeral() {
        if (toque.consumirPausa() || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            pausado = !pausado;
            jogo.sons().tocar("clique", 0.6f);
        }
        if (pausado && Gdx.input.justTouched() && hud.toqueMenuNaPausa()) {
            jogo.trocarTela(new TelaMenu(jogo));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pausado = !pausado;
            jogo.sons().tocar("clique", 0.6f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            jogo.sons().alternarMudo();
        }
        boolean querPoder = toque.consumirPoder() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT);
        if (querPoder && !pausado) {
            partida.ativarPoder();
        }
        if (pausado && Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            jogo.trocarTela(new TelaMenu(jogo));
            return;
        }
        if (partida.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
            int clicado = hud.cartaoClicado();
            for (int i = 0; i < partida.opcoesUpgrade().size(); i++) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i) || clicado == i) {
                    var escolhida = partida.opcoesUpgrade().get(i);
                    partida.escolherUpgrade(i);
                    jogo.sons().tocar("clique", 0.8f);
                    if (!escolhida.ehEvolucao() && escolhida.raridade() == br.edu.unifor.missaomarte.sobrevivencia.upgrades.Raridade.LENDARIO) {
                        efeitoLendario(escolhida.titulo(partida));
                    }
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
                case PODER_ATIVADO -> {
                    s.tocar("investida", 0.8f);
                    tremor = Math.min(1f, tremor + 0.25f);
                }
                case CHEFE_MIRANDO -> s.tocar("alerta", 0.6f);
                case CHEFE_INVESTIU -> {
                    s.tocar("chefe", 0.5f, 0.5f);
                    tremor = Math.min(1f, tremor + 0.4f);
                }
                case CHEFE_ANEL -> s.tocar("explosao", 0.6f);
                case MISSIL_LANCADO -> s.tocar("missil", 0.35f, 0.2f);
                case RAIO_DISPARADO -> {
                    s.tocar("raio", 0.4f, 0.15f);
                    efeitoRaio();
                }
                case NAVE_CURADA -> {
                    Vetor p = partida.nave().posicao();
                    particulas.subir(p.x(), -p.y(), 9, 40);
                    particulas.subir(p.x(), -p.y(), 8, 12);
                    acenderFlash(p.x(), -p.y(), 0.3f, 1f, 0.45f, 25f);
                    s.tocar("resgate", 0.7f);
                }
                case EVOLUCAO -> {
                    s.tocar("evolucao", 1f);
                    tremor = 1f;
                    Vetor p = partida.nave().posicao();
                    particulas.explosao(p.x(), -p.y(), 4, 50, 10f);
                }
                default -> { }
            }
            hud.notificar(e);
        }
    }

    /** Faíscas em cada inimigo atingido pelo raio; na Tempestade Elétrica, clarão e tremor. */
    private void efeitoRaio() {
        partida.arma(RaioEmCadeia.class).ifPresent(r -> {
            boolean evo = r.evoluida();
            List<Vetor> c = r.ultimoCaminho();
            for (int k = 1; k < c.size(); k++) {
                Vetor v = c.get(k);
                particulas.faiscas(v.x(), 1f, -v.y(), 7, evo ? 14 : 6, evo ? 9f : 6f);
                particulas.faiscas(v.x(), 1f, -v.y(), 8, evo ? 8 : 3, evo ? 7f : 5f);
            }
            if (evo && c.size() > 1) {
                Vetor meio = c.get(c.size() / 2);
                acenderFlash(meio.x(), -meio.y(), 0.7f, 0.6f, 1f, 45f);
                tremor = Math.min(1f, tremor + 0.12f);
            }
        });
    }

    private void efeitoLendario(String titulo) {
        Vetor p = partida.nave().posicao();
        particulas.subir(p.x(), -p.y(), 10, 50);
        particulas.faiscas(p.x(), 1f, -p.y(), 8, 30, 10f);
        acenderFlash(p.x(), -p.y(), 1f, 0.8f, 0.3f, 40f);
        jogo.sons().tocar("evolucao", 0.6f, 0.2f);
        hud.lendaria(titulo);
    }

    private void acenderFlash(float x, float z, float r, float g, float b, float intensidade) {
        mundo.luzEfeito.position.set(x, 2f, z);
        mundo.luzEfeito.color.set(r, g, b, 1f);
        picoFlash = intensidade;
        flash = 1f;
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
        boolean investindo = partida.poder().emAcao();
        boolean piscando = partida.nave().invulneravel() && !investindo && ((int) (relogio * 20) % 2 == 0);
        if (partida.estado() != EstadoPartida.DERROTA && !(piscando && env != null)) {
            float flutuar = 0.8f + MathUtils.sin(relogio * 3f) * 0.12f;
            ModelInstance n = mundo.kenney.nave.em(0, nave.x(), flutuar, -nave.y(), angulo(partida.nave().direcao()), 1f);
            n.transform.rotate(Vector3.X, naveAndando || investindo ? -8f : 0f);
            if (investindo) {
                n.transform.scale(0.85f, 0.85f, 1.5f);
            }
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
        boolean plasma = partida.arma(br.edu.unifor.missaomarte.sobrevivencia.armas.TiroAutomatico.class)
                .map(br.edu.unifor.missaomarte.sobrevivencia.armas.Arma::evoluida).orElse(false);
        float escTiro = plasma ? 1.7f : 1f;
        for (Projetil p : partida.projeteis()) {
            iProjetil.transform.setToTranslation(p.posicao().x(), 0.8f, -p.posicao().y()).rotate(Vector3.Y, angulo(p.velocidade()))
                    .scale(escTiro, escTiro, escTiro);
            batch.render(iProjetil, env);
            if (plasma && env != null && MathUtils.randomBoolean(0.25f)) {
                particulas.faiscas(p.posicao().x(), 0.8f, -p.posicao().y(), 5, 1, 1.5f);
            }
        }
        Vetor pos = partida.nave().posicao();
        partida.arma(Orbital.class).ifPresent(o -> {
            float esc = o.raioEsfera() / Orbital.RAIO_ESFERA;
            for (Vetor e : o.posicoes(pos)) {
                iOrbital.transform.setToTranslation(e.x(), 0.8f, -e.y()).rotate(Vector3.Y, relogio * 200f).scale(esc, esc, esc);
                batch.render(iOrbital, env);
                if (o.evoluida() && env != null && MathUtils.randomBoolean(0.5f)) {
                    particulas.faiscas(e.x(), 0.8f, -e.y(), 7, 1, 2f);
                }
            }
        });
        partida.arma(MisseisTeleguiados.class).ifPresent(mt -> {
            java.util.Set<MisseisTeleguiados.Missil> agora = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
            float escala = mt.evoluida() ? 1.8f : 1f;
            for (MisseisTeleguiados.Missil mi : mt.emVoo()) {
                agora.add(mi);
                ultimaPosMissil.put(mi, mi.posicao());
                iMissil.transform.setToTranslation(mi.posicao().x(), 1.1f, -mi.posicao().y()).rotate(Vector3.Y, angulo(mi.direcao()))
                        .scale(escala, escala, escala);
                batch.render(iMissil, env);
                float tx = mi.posicao().x() - mi.direcao().x() * 0.6f;
                float tz = -(mi.posicao().y() - mi.direcao().y() * 0.6f);
                if (mt.evoluida()) {
                    // meteoro: bola de fogo pulsando em volta do míssil e rastro grosso
                    float b = 1.3f + 0.2f * MathUtils.sin(relogio * 25f + mi.posicao().x());
                    iBrilhoFogo.transform.setToTranslation(mi.posicao().x(), 1.1f, -mi.posicao().y()).scale(b, b, b);
                    batch.render(iBrilhoFogo, env);
                    if (env != null) {
                        particulas.rastroFogo(tx, 1.1f, tz);
                    }
                } else if (env != null && MathUtils.randomBoolean(0.6f)) {
                    particulas.rastro(tx, 1.1f, tz);
                }
            }
            if (env != null) {
                for (MisseisTeleguiados.Missil antigo : misseisVistos) {
                    if (!agora.contains(antigo)) {
                        Vetor ex = ultimaPosMissil.remove(antigo);
                        if (ex != null) {
                            if (mt.evoluida()) {
                                particulas.explosao(ex.x(), -ex.y(), 4, 40, 13f);
                                particulas.explosao(ex.x(), -ex.y(), 10, 16, 9f);
                                particulas.faiscas(ex.x(), 0.8f, -ex.y(), 8, 14, 12f);
                                explosoesFogo.add(new float[] {ex.x(), -ex.y(), 0f, mt.raioExplosao(partida)});
                                acenderFlash(ex.x(), -ex.y(), 1f, 0.5f, 0.12f, 50f);
                                tremor = Math.min(1f, tremor + 0.15f);
                                jogo.sons().tocar("explosao_grande", 0.35f, 0.1f);
                            } else {
                                particulas.explosao(ex.x(), -ex.y(), 4, 12, 7f);
                                jogo.sons().tocar("explosao", 0.3f, 0.05f);
                            }
                        }
                    }
                }
                misseisVistos.clear();
                misseisVistos.addAll(agora);
            }
        });
        for (float[] e : explosoesFogo) {
            float prog = Math.min(1f, e[2] / 0.5f);
            float raio = e[3] * com.badlogic.gdx.math.Interpolation.pow2Out.apply(prog);
            BlendingAttribute bl = (BlendingAttribute) iAnelFogo.materials.get(0).get(BlendingAttribute.Type);
            bl.opacity = 0.75f * (1 - prog);
            iAnelFogo.transform.setToTranslation(e[0], 0.25f, e[1]).scale(raio, 1f, raio);
            batch.render(iAnelFogo, env);
            float bola = e[3] * 0.7f * (1 - prog);
            if (bola > 0.05f) {
                iBrilhoFogo.transform.setToTranslation(e[0], 0.6f, e[1]).scale(bola, bola * 0.7f, bola);
                batch.render(iBrilhoFogo, env);
            }
        }
        partida.arma(RaioEmCadeia.class).ifPresent(r -> {
            boolean evo = r.evoluida();
            float duracao = evo ? 0.3f : 0.18f;
            if (r.tempoDesdeDisparo() < duracao && r.ultimoCaminho().size() > 1) {
                java.util.List<Vetor> c = r.ultimoCaminho();
                float vida = 1f - r.tempoDesdeDisparo() / duracao;
                // na evolução o raio tremeluz: alguns quadros some e volta
                if (evo && vida < 0.7f && MathUtils.randomBoolean(0.2f)) {
                    return;
                }
                float espessura = (evo ? 0.2f : 0.12f) * vida + 0.04f;
                float desvio = evo ? 0.8f : 0.5f;
                for (int k = 0; k + 1 < c.size(); k++) {
                    Vetor a = c.get(k);
                    Vetor bb = c.get(k + 1);
                    // segmento em zigue-zague com pontos de desvio no meio
                    int partes = evo ? 4 : 2;
                    Vetor anterior = a;
                    for (int q = 1; q <= partes; q++) {
                        Vetor prox = q == partes ? bb : a.mais(bb.menos(a).vezes(q / (float) partes))
                                .mais(new Vetor(MathUtils.random(-desvio, desvio), MathUtils.random(-desvio, desvio)));
                        segmentoRaio(batch, env, anterior, prox, espessura, evo);
                        if (evo && q < partes && MathUtils.randomBoolean(0.45f)) {
                            // galhos finos saindo do raio principal
                            Vetor galho = prox.mais(new Vetor(MathUtils.random(-1.6f, 1.6f), MathUtils.random(-1.6f, 1.6f)));
                            segmentoRaio(batch, env, prox, galho, espessura * 0.45f, true);
                        }
                        anterior = prox;
                    }
                }
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
                if (o.evoluida()) {
                    // Terremoto Marciano: segundo anel atrasado e poeira levantando na borda
                    float r2 = o.raio() * Math.max(0, progresso - 0.25f) / 0.75f;
                    b.opacity = 0.45f * (1 - progresso);
                    iOnda.transform.setToTranslation(pos.x(), 0.35f, -pos.y()).scale(r2, 1.5f, r2);
                    batch.render(iOnda, env);
                    if (env != null) {
                        for (int k = 0; k < 4; k++) {
                            float a = MathUtils.random(MathUtils.PI2);
                            particulas.faiscas(pos.x() + MathUtils.cos(a) * r, 0.3f, -pos.y() + MathUtils.sin(a) * r, 4, 1, 3f);
                        }
                    }
                }
            }
        });
        Vetor alvo = partida.plataformaAberta() ? Vetor.ZERO
                : partida.passageiroMaisProximo().map(Passageiro::posicao).orElse(null);
        if (alvo != null && alvo.distancia(pos) > 4f) {
            Vetor d = alvo.menos(pos).normalizado();
            iSeta.transform.setToTranslation(pos.x() + d.x() * 2.4f, 0.15f, -(pos.y() + d.y() * 2.4f)).rotate(Vector3.Y, angulo(d));
            batch.render(iSeta, env);
        }
        for (ProjetilInimigo p : partida.projeteisInimigos()) {
            float s = 1f + 0.15f * MathUtils.sin(relogio * 20f + p.posicao().x());
            iProjetilInimigo.transform.setToTranslation(p.posicao().x(), 1f, -p.posicao().y()).scale(s, s, s);
            batch.render(iProjetilInimigo, env);
        }
        for (Inimigo i : partida.inimigos()) {
            ComportamentoChefe c = i.comportamento();
            if (c != null && c.estado() == ComportamentoChefe.Estado.MIRAR) {
                // faixa vermelha no chão mostrando por onde o chefe vai investir
                Vetor d = c.direcaoTravada();
                float comprimento = 10f * (0.3f + 0.7f * c.progresso());
                Vetor meio = i.posicao().mais(d.vezes(comprimento / 2f));
                BlendingAttribute b = (BlendingAttribute) iAviso.materials.get(0).get(BlendingAttribute.Type);
                b.opacity = 0.25f + 0.35f * Math.abs(MathUtils.sin(relogio * 14f));
                iAviso.transform.setToTranslation(meio.x(), 0.06f, -meio.y()).rotate(Vector3.Y, angulo(d))
                        .scale(3.6f, 1f, comprimento);
                batch.render(iAviso, env);
            }
        }
        if (partida.poder().emAcao() && env != null) {
            Vetor p = partida.nave().posicao();
            for (int k = 0; k < 3; k++) {
                particulas.rastro(p.x() + MathUtils.random(-0.4f, 0.4f), 0.8f, -p.y() + MathUtils.random(-0.4f, 0.4f));
            }
        }
        particulas.desenhar(batch, env);
        particulas.desenharPoeira(batch, env, alvoCamera.x, alvoCamera.z, relogio);
    }

    /** Um pedaço do raio: núcleo branco, corpo azul e (na evolução) aura roxa larga. */
    private void segmentoRaio(ModelBatch batch, Environment env, Vetor a, Vetor b, float espessura, boolean evo) {
        Vetor d = b.menos(a);
        float comp = d.comprimento();
        if (comp < 0.01f) {
            return;
        }
        Vetor m = a.mais(d.vezes(0.5f));
        float ang = angulo(d);
        if (evo) {
            iRaioAura.transform.setToTranslation(m.x(), 1.0f, -m.y()).rotate(Vector3.Y, ang).scale(espessura * 3.5f, espessura * 3.5f, comp);
            batch.render(iRaioAura, env);
        }
        iRaio.transform.setToTranslation(m.x(), 1.0f, -m.y()).rotate(Vector3.Y, ang).scale(espessura * 1.6f, espessura * 1.6f, comp);
        batch.render(iRaio, env);
        iRaioNucleo.transform.setToTranslation(m.x(), 1.0f, -m.y()).rotate(Vector3.Y, ang).scale(espessura * 0.5f, espessura * 0.5f, comp);
        batch.render(iRaioNucleo, env);
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
        mundo.luzEfeito.intensity = picoFlash * flash;
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

    private float esperaCartas;
    private boolean cartasCapturadas;

    private Vetor pilotoAutomatico() {
        if (partida.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
            if (!cartasCapturadas) {
                esperaCartas += Gdx.graphics.getDeltaTime();
                if (esperaCartas < 1.2f) {
                    return Vetor.ZERO;
                }
                jogo.salvarCaptura("captura-cartas.png");
                cartasCapturadas = true;
            }
            partida.escolherUpgrade(0);
        }
        Vetor pos = partida.nave().posicao();
        Vetor alvo = partida.passageiroMaisProximo().map(Passageiro::posicao).orElse(Vetor.polar(relogio * 0.5f, 8f));
        Vetor dir = alvo.menos(pos).normalizado();
        long perto = partida.inimigos().stream().filter(i -> i.posicao().distancia(pos) < 3f).count();
        if (perto >= 3) {
            partida.ativarPoder();
        }
        for (Inimigo i : partida.inimigos()) {
            float d = i.posicao().distancia(pos);
            if (d < 4f) {
                dir = dir.mais(pos.menos(i.posicao()).normalizado().vezes((4f - d) / 2f));
            }
        }
        return dir;
    }

    private boolean demoEvoluiu;

    /** No CI: aos 10 s entrega todas as armas evoluídas, para as capturas mostrarem os efeitos completos. */
    private void evoluirTudoNaDemonstracao() {
        demoEvoluiu = true;
        var r = br.edu.unifor.missaomarte.sobrevivencia.upgrades.Raridade.LENDARIO;
        java.util.List<br.edu.unifor.missaomarte.sobrevivencia.upgrades.Upgrade> catalogo = java.util.List.of(
                new br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeArma<>(br.edu.unifor.missaomarte.sobrevivencia.armas.TiroAutomatico.class,
                        br.edu.unifor.missaomarte.sobrevivencia.armas.TiroAutomatico::new, "", "tiro"),
                new br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeArma<>(Orbital.class, Orbital::new, "", "orbital"),
                new br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeArma<>(OndaDeChoque.class, OndaDeChoque::new, "", "onda"),
                new br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeArma<>(MisseisTeleguiados.class, MisseisTeleguiados::new, "", "missil"),
                new br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeArma<>(RaioEmCadeia.class, RaioEmCadeia::new, "", "raio"));
        for (var u : catalogo) {
            for (int k = 0; k < 5 && u.disponivel(partida); k++) {
                u.aplicar(partida, r);
            }
        }
        for (var e : br.edu.unifor.missaomarte.sobrevivencia.upgrades.Evolucao.values()) {
            e.aplicar(partida, r);
        }
        for (int k = 0; k < 5; k++) {
            br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeAtributo.VIDA_MAXIMA.aplicar(partida, r);
        }
    }

    private void capturarDemonstracao() {
        if (!demoEvoluiu && relogio > 10f && partida.estado() == EstadoPartida.EM_ANDAMENTO) {
            evoluirTudoNaDemonstracao();
        }
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
