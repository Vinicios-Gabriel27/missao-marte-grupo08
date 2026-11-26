package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.Arena;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Fundo animado do menu e da tela final: a nave sobrevoa a base, passageiros acenam na
 * plataforma e alienígenas rondam, com a câmera girando devagar.
 */
class CenaVitrine {
    private final Mundo3D mundo;
    private final Cenario cenario;
    private final Particulas particulas;
    private final Vector3 centro = new Vector3();
    private float tempo;
    private float rastro;

    CenaVitrine(Mundo3D mundo) {
        this.mundo = mundo;
        this.cenario = new Cenario(mundo.modelos, mundo.kenney, Arena.PADRAO, 42L);
        this.particulas = new Particulas(mundo.modelos);
    }

    void desenhar(float delta) {
        tempo += delta;
        mundo.kenney.atualizar(delta);
        particulas.atualizar(delta);

        float angCam = tempo * 0.08f;
        mundo.camera.position.set(MathUtils.cos(angCam) * 17f, 8.5f, MathUtils.sin(angCam) * 17f);
        mundo.camera.up.set(Vector3.Y);
        mundo.camera.lookAt(0, 1.2f, 0);
        mundo.camera.update();

        float angNave = tempo * 0.55f;
        float nx = MathUtils.cos(angNave) * 6.5f;
        float nz = MathUtils.sin(angNave) * 6.5f;
        float yawNave = -angNave * MathUtils.radiansToDegrees;
        float alturaNave = 2.2f + MathUtils.sin(tempo * 1.7f) * 0.3f;
        rastro -= delta;
        if (rastro <= 0) {
            float tx = MathUtils.cos(angNave - 0.2f) * 6.5f;
            float tz = MathUtils.sin(angNave - 0.2f) * 6.5f;
            particulas.rastro(tx, alturaNave + 0.1f, tz);
            rastro = 0.025f;
        }
        mundo.luzMotor.position.set(nx, alturaNave, nz);
        mundo.luzMotor.intensity = 9f;
        mundo.luzOrbital.intensity = 0f;
        mundo.luzPlataforma.color.set(0.2f, 1f, 0.35f, 1f);
        mundo.luzPlataforma.intensity = 16f + MathUtils.sin(tempo * 3f) * 4f;

        mundo.desenhar(centro, cenario.instanciasPbr, (b, env) -> {
            b.render(mundo.kenney.plataforma.em(0, 0, 0, 0, 0, 1f), env);
            ModelInstance n = mundo.kenney.nave.em(0, nx, alturaNave, nz, yawNave, 1.1f);
            n.transform.rotate(Vector3.Z, 18f);
            b.render(n, env);
            b.render(mundo.kenney.professor.em(0, -1.2f, 0.1f, 0.6f, 20f, 1f), env);
            b.render(mundo.kenney.engenheiro.em(1, 0.3f, 0.1f, 1.2f, -10f, 1f), env);
            b.render(mundo.kenney.astronauta.em(2, 1.3f, 0.1f, -0.2f, -40f, 1f), env);
            for (int i = 0; i < 6; i++) {
                float a = -tempo * 0.25f + i * MathUtils.PI2 / 6f;
                float x = MathUtils.cos(a) * 11f;
                float z = MathUtils.sin(a) * 11f;
                Modelo3D m = mundo.kenney.inimigos[i % 3];
                b.render(m.em(i, x, 0, z, -a * MathUtils.radiansToDegrees + 180f, 1f), env);
            }
        }, (b, env) -> {
            particulas.desenhar(b, env);
            particulas.desenharPoeira(b, env, 0, 0, tempo);
        });
    }
}
