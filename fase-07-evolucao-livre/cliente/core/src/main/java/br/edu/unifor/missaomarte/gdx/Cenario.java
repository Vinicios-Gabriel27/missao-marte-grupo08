package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.Arena;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

/** Chão de Marte, rochas, crateras e muros da arena. Só decoração: não afeta o jogo. */
class Cenario {
    /** Modelos simples (chão, montanhas, muros) — sombreamento clássico. */
    final Array<ModelInstance> instancias = new Array<>();
    /** Modelos Kenney (rochas, crateras, estruturas) — sombreamento PBR. */
    final Array<ModelInstance> instanciasPbr = new Array<>();

    Cenario(Modelos modelos, ModelosKenney kenney, Arena arena, long semente) {
        float m = arena.meiaLargura();
        Random r = new Random(semente);

        ModelInstance chao = new ModelInstance(modelos.chao);
        chao.transform.setToTranslation(0, 0f, 0).scale(2 * m + 140, 1f, 2 * m + 140);
        instanciasPbr.add(chao);

        for (int i = 0; i < 22; i++) {
            Model cratera = kenney.crateras.get(i % kenney.crateras.size);
            float d = 3f + r.nextFloat() * 5f;
            ModelInstance c = new ModelInstance(cratera);
            c.transform.setToTranslation(sortear(r, m, 6), -0.05f, sortear(r, m, 6))
                    .rotate(Vector3.Y, r.nextFloat() * 360f).scale(d, d * 0.6f, d);
            instanciasPbr.add(c);
        }
        // Dentro da arena: só pedras pequenas (não escondem inimigos)
        for (int i = 0; i < 45; i++) {
            Model rocha = kenney.rochas.get(3 + r.nextInt(2)); // rocks_smallA/B
            float s = 0.8f + r.nextFloat() * 1.2f;
            ModelInstance pedra = new ModelInstance(rocha);
            pedra.transform.setToTranslation(sortear(r, m - 1, 4), -0.02f, sortear(r, m - 1, 4))
                    .rotate(Vector3.Y, r.nextFloat() * 360f).scale(s, s, s);
            instanciasPbr.add(pedra);
        }
        // Fora da arena: rochas grandes, cristais e a base marciana (cenário de fundo)
        for (int i = 0; i < 70; i++) {
            Model rocha = kenney.rochas.get(r.nextInt(kenney.rochas.size));
            float s = 2.5f + r.nextFloat() * 4f;
            float ang = r.nextFloat() * MathUtils.PI2;
            float dist = m + 3f + r.nextFloat() * 14f;
            ModelInstance pedra = new ModelInstance(rocha);
            pedra.transform.setToTranslation(foraX(ang, dist, m), -0.05f, foraZ(ang, dist, m))
                    .rotate(Vector3.Y, r.nextFloat() * 360f).scale(s, s, s);
            instanciasPbr.add(pedra);
        }
        for (int i = 0; i < 14; i++) {
            ModelInstance e = new ModelInstance(kenney.estruturas.get(i % kenney.estruturas.size));
            float ang = i / 14f * MathUtils.PI2 + 0.2f;
            float dist = m + 5f + r.nextFloat() * 4f;
            float s = 3f + r.nextFloat() * 2.5f;
            e.transform.setToTranslation(foraX(ang, dist, m), 0f, foraZ(ang, dist, m))
                    .rotate(Vector3.Y, r.nextFloat() * 360f).scale(s, s, s);
            instanciasPbr.add(e);
        }
        for (int i = 0; i < 40; i++) {
            float ang = i / 40f * MathUtils.PI2 + r.nextFloat() * 0.1f;
            float dist = m + 18 + r.nextFloat() * 25;
            float alt = 6 + r.nextFloat() * 14;
            float larg = 10 + r.nextFloat() * 14;
            ModelInstance mont = new ModelInstance(modelos.montanha);
            mont.transform.setToTranslation(MathUtils.cos(ang) * dist, alt / 2 - 0.5f, MathUtils.sin(ang) * dist)
                    .rotate(Vector3.Y, r.nextFloat() * 360).scale(larg, alt, larg * (0.7f + r.nextFloat() * 0.6f));
            instanciasPbr.add(mont);
        }
        float espessura = 1f;
        float altura = 1.2f;
        float comprimento = 2 * m + 2 * espessura;
        adicionarMuro(modelos, 0, m + espessura / 2, comprimento, altura, espessura);
        adicionarMuro(modelos, 0, -m - espessura / 2, comprimento, altura, espessura);
        adicionarMuro(modelos, m + espessura / 2, 0, espessura, altura, comprimento);
        adicionarMuro(modelos, -m - espessura / 2, 0, espessura, altura, comprimento);
    }

    private void adicionarMuro(Modelos modelos, float x, float z, float largura, float altura, float profundidade) {
        ModelInstance muro = new ModelInstance(modelos.muro);
        muro.transform.setToTranslation(x, altura / 2, z).scale(largura, altura, profundidade);
        instanciasPbr.add(muro);
    }

    /** Ponto na direção do ângulo, empurrado para fora do quadrado da arena. */
    private static float foraX(float ang, float dist, float m) {
        float c = MathUtils.cos(ang);
        float sn = MathUtils.sin(ang);
        float k = Math.max(Math.abs(c), Math.abs(sn));
        return c / k * dist;
    }

    private static float foraZ(float ang, float dist, float m) {
        float c = MathUtils.cos(ang);
        float sn = MathUtils.sin(ang);
        float k = Math.max(Math.abs(c), Math.abs(sn));
        return sn / k * dist;
    }

    /** Posição aleatória em [-limite, limite], evitando o centro (plataforma). */
    private static float sortear(Random r, float limite, float evitarCentro) {
        float v;
        do {
            v = (r.nextFloat() * 2 - 1) * limite;
        } while (Math.abs(v) < evitarCentro && r.nextBoolean());
        return MathUtils.clamp(v, -limite, limite);
    }
}
