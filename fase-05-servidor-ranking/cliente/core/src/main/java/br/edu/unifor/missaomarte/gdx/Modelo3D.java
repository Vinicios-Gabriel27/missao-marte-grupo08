package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Um modelo glTF pronto para desenhar: normaliza tamanho e "pé no chão", corrige a frente
 * e mantém algumas variantes com a animação em fases diferentes, para a horda não andar
 * toda sincronizada.
 */
class Modelo3D {
    private final ModelInstance[] variantes;
    private final AnimationController[] animacoes;
    private final float escala;
    private final float yawBase;
    private final Vector3 ajuste = new Vector3();

    /**
     * @param tamanhoAlvo altura (se porAltura) ou maior dimensão do modelo em unidades do mundo
     * @param yawBase     rotação extra para a frente do modelo apontar para +Z
     * @param animacao    nome da animação em loop, ou null
     */
    Modelo3D(Model model, float tamanhoAlvo, boolean porAltura, float yawBase, int quantidadeVariantes, String animacao) {
        // Mede pela instância (já com as transformações dos nós): alguns modelos têm a origem num canto.
        ModelInstance medida = new ModelInstance(model);
        medida.calculateTransforms();
        BoundingBox caixa = medida.calculateBoundingBox(new BoundingBox());
        float referencia = porAltura ? caixa.getHeight() : Math.max(caixa.getWidth(), caixa.getDepth());
        this.escala = tamanhoAlvo / Math.max(referencia, 1e-4f);
        this.yawBase = yawBase;
        Vector3 centro = caixa.getCenter(new Vector3());
        ajuste.set(-centro.x, -caixa.min.y, -centro.z);

        variantes = new ModelInstance[quantidadeVariantes];
        animacoes = new AnimationController[quantidadeVariantes];
        for (int i = 0; i < quantidadeVariantes; i++) {
            variantes[i] = new ModelInstance(model);
            if (animacao != null && variantes[i].getAnimation(animacao) != null) {
                animacoes[i] = new AnimationController(variantes[i]);
                animacoes[i].setAnimation(animacao, -1);
                animacoes[i].update(i * 0.29f);
            }
        }
    }

    void atualizar(float delta) {
        for (AnimationController a : animacoes) {
            if (a != null) {
                a.update(delta);
            }
        }
    }

    void trocarAnimacao(String nome) {
        for (int i = 0; i < variantes.length; i++) {
            if (animacoes[i] != null && variantes[i].getAnimation(nome) != null) {
                animacoes[i].animate(nome, -1, 1f, null, 0.2f);
            }
        }
    }

    /** Posiciona a variante {@code indice} e devolve a instância para desenhar. */
    ModelInstance em(int indice, float x, float y, float z, float yawGraus, float escalaExtra) {
        ModelInstance inst = variantes[Math.floorMod(indice, variantes.length)];
        float e = escala * escalaExtra;
        inst.transform.setToTranslation(x, y, z).rotate(Vector3.Y, yawGraus + yawBase).scale(e, e, e).translate(ajuste);
        return inst;
    }
}
