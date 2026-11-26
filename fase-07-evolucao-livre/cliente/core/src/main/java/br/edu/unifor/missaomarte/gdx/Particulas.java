package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Efeitos visuais simples (explosões e rastro). Puro enfeite: vive só na apresentação.
 * Cada cor tem sua instância de cubinho, porque o ModelBatch desenha com o material
 * do momento em que o quadro termina.
 */
class Particulas {
    private static final class Particula {
        final Vector3 pos = new Vector3();
        final Vector3 vel = new Vector3();
        float vida;
        float vidaTotal;
        float tamanho;
        float giro;
        int cor;
        boolean gravidade;
    }

    private final Array<Particula> ativas = new Array<>(false, 512);
    private final Pool<Particula> pool = new Pool<>() {
        @Override
        protected Particula newObject() {
            return new Particula();
        }
    };
    private final Array<ModelInstance> porCor = new Array<>();
    private final ModelInstance iPoeira;
    private final float[] poeira = new float[3 * 140];
    private final Array<Color> cores = new Array<>();

    Particulas(Modelos modelos) {
        iPoeira = new ModelInstance(modelos.poeira);
        for (int i = 0; i < poeira.length; i += 3) {
            poeira[i] = MathUtils.random(-22f, 22f);
            poeira[i + 1] = MathUtils.random(0.2f, 6f);
            poeira[i + 2] = MathUtils.random(-22f, 22f);
        }
        for (Color c : new Color[] {
            new Color(0.95f, 0.2f, 0.2f, 1f),   // 0 perseguidor
            new Color(1f, 0.85f, 0.15f, 1f),    // 1 rápido
            new Color(0.6f, 0.3f, 0.9f, 1f),    // 2 tanque
            new Color(0.6f, 0.05f, 0.05f, 1f),  // 3 chefe
            new Color(1f, 0.6f, 0.15f, 1f),     // 4 fogo/motor
            new Color(0.4f, 1f, 1f, 1f),        // 5 faísca de tiro
            new Color(0.5f, 1f, 0.5f, 1f),      // 6 XP
            new Color(0.7f, 0.55f, 1f, 1f),     // 7 elétrico
            new Color(1f, 1f, 1f, 1f),          // 8 branco
            new Color(0.3f, 1f, 0.45f, 1f),     // 9 cura
            new Color(1f, 0.85f, 0.3f, 1f),     // 10 dourado
        }) {
            ModelInstance inst = new ModelInstance(modelos.particula);
            inst.materials.get(0).set(ColorAttribute.createDiffuse(c), ColorAttribute.createEmissive(c));
            porCor.add(inst);
            cores.add(c);
        }
    }

    void explosao(float x, float z, int cor, int quantidade, float forca) {
        for (int i = 0; i < quantidade; i++) {
            Particula p = pool.obtain();
            p.pos.set(x, 0.6f, z);
            p.vel.set(MathUtils.random(-1f, 1f), MathUtils.random(0.6f, 1.6f), MathUtils.random(-1f, 1f)).nor().scl(forca * MathUtils.random(0.5f, 1f));
            p.vidaTotal = p.vida = MathUtils.random(0.4f, 0.8f);
            p.tamanho = MathUtils.random(0.15f, 0.35f) * (forca > 8 ? 2f : 1f);
            p.giro = MathUtils.random(360f);
            p.cor = cor;
            p.gravidade = true;
            ativas.add(p);
        }
        for (int i = 0; i < quantidade / 2; i++) {
            Particula p = pool.obtain();
            p.pos.set(x, 0.6f, z);
            p.vel.set(MathUtils.random(-1f, 1f), MathUtils.random(0f, 0.5f), MathUtils.random(-1f, 1f)).nor().scl(forca * 0.4f);
            p.vidaTotal = p.vida = 0.3f;
            p.tamanho = 0.5f;
            p.cor = 4;
            p.gravidade = false;
            ativas.add(p);
        }
    }

    /** Faíscas rápidas, sem fogo (raio, brilho de carta lendária). */
    void faiscas(float x, float y, float z, int cor, int quantidade, float forca) {
        for (int i = 0; i < quantidade; i++) {
            Particula p = pool.obtain();
            p.pos.set(x, y, z);
            p.vel.set(MathUtils.random(-1f, 1f), MathUtils.random(-0.3f, 1f), MathUtils.random(-1f, 1f)).nor().scl(forca * MathUtils.random(0.4f, 1f));
            p.vidaTotal = p.vida = MathUtils.random(0.15f, 0.35f);
            p.tamanho = MathUtils.random(0.08f, 0.18f);
            p.giro = MathUtils.random(360f);
            p.cor = cor;
            p.gravidade = false;
            ativas.add(p);
        }
    }

    /** Brilhos subindo em espiral em volta de um ponto (cura). */
    void subir(float x, float z, int cor, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            Particula p = pool.obtain();
            float a = MathUtils.random(MathUtils.PI2);
            float r = MathUtils.random(0.4f, 1.3f);
            p.pos.set(x + MathUtils.cos(a) * r, MathUtils.random(0.2f, 1.2f), z + MathUtils.sin(a) * r);
            p.vel.set(-MathUtils.sin(a) * 1.2f, MathUtils.random(2f, 4f), MathUtils.cos(a) * 1.2f);
            p.vidaTotal = p.vida = MathUtils.random(0.6f, 1.1f);
            p.tamanho = MathUtils.random(0.12f, 0.26f);
            p.giro = MathUtils.random(360f);
            p.cor = cor;
            p.gravidade = false;
            ativas.add(p);
        }
    }

    /** Rastro de fogo grosso (meteoro). */
    void rastroFogo(float x, float y, float z) {
        for (int i = 0; i < 3; i++) {
            Particula p = pool.obtain();
            p.pos.set(x + MathUtils.random(-0.25f, 0.25f), y + MathUtils.random(-0.2f, 0.2f), z + MathUtils.random(-0.25f, 0.25f));
            p.vel.set(MathUtils.random(-0.4f, 0.4f), MathUtils.random(0.5f, 1.5f), MathUtils.random(-0.4f, 0.4f));
            p.vidaTotal = p.vida = MathUtils.random(0.35f, 0.6f);
            p.tamanho = MathUtils.random(0.35f, 0.6f);
            p.giro = MathUtils.random(360f);
            p.cor = i == 0 ? 10 : 4;
            p.gravidade = false;
            ativas.add(p);
        }
    }

    void rastro(float x, float y, float z) {
        Particula p = pool.obtain();
        p.pos.set(x + MathUtils.random(-0.08f, 0.08f), y, z + MathUtils.random(-0.08f, 0.08f));
        p.vel.set(0, 0.3f, 0);
        p.vidaTotal = p.vida = 0.35f;
        p.tamanho = 0.28f;
        p.cor = 4;
        p.gravidade = false;
        ativas.add(p);
    }

    void atualizar(float delta) {
        for (int i = ativas.size - 1; i >= 0; i--) {
            Particula p = ativas.get(i);
            p.vida -= delta;
            if (p.vida <= 0) {
                ativas.removeIndex(i);
                pool.free(p);
                continue;
            }
            if (p.gravidade) {
                p.vel.y -= 18f * delta;
            }
            p.pos.mulAdd(p.vel, delta);
            if (p.pos.y < 0.05f) {
                p.pos.y = 0.05f;
                p.vel.scl(0.5f, -0.3f, 0.5f);
            }
            p.giro += 400f * delta;
        }
    }

    /** Grãos de poeira flutuando em volta do centro (seguem a câmera, dando ao ar uma "textura"). */
    void desenharPoeira(ModelBatch batch, Environment ambiente, float cx, float cz, float tempo) {
        for (int i = 0; i < poeira.length; i += 3) {
            float x = cx + envolver(poeira[i] + tempo * 0.9f - cx, 22f);
            float z = cz + envolver(poeira[i + 2] + tempo * 0.4f - cz, 22f);
            float y = poeira[i + 1] + MathUtils.sin(tempo * 0.8f + i) * 0.3f;
            float s = 0.05f + (i % 7) * 0.012f;
            iPoeira.transform.setToTranslation(x, y, z).scale(s, s, s);
            batch.render(iPoeira, ambiente);
        }
    }

    private static float envolver(float v, float meio) {
        float t = 2 * meio;
        return ((v + meio) % t + t) % t - meio;
    }

    void desenhar(ModelBatch batch, Environment ambiente) {
        for (Particula p : ativas) {
            float s = p.tamanho * (p.vida / p.vidaTotal);
            ModelInstance inst = porCor.get(p.cor);
            inst.transform.setToTranslation(p.pos).rotate(Vector3.Y, p.giro).rotate(Vector3.X, p.giro * 0.7f).scale(s, s, s);
            batch.render(inst, ambiente);
        }
    }
}
