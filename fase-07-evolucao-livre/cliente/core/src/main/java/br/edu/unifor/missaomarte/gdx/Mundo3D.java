package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
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
 * Infraestrutura 3D compartilhada pelas telas (criada uma vez): modelos, renderização PBR e
 * clássica, sol com sombras, luz de ambiente em tons de Marte, névoa, luzes dinâmicas e
 * pós-processamento (bloom, cor, vinheta). Cada tela só diz o que desenhar.
 */
class Mundo3D implements Disposable {
    static final Color CEU = new Color(0.62f, 0.36f, 0.26f, 1f);

    /** O que uma tela desenha numa passada (o ambiente é null na passada de sombras). */
    interface Desenho {
        void desenhar(ModelBatch batch, Environment ambiente);
    }

    final Modelos modelos = new Modelos();
    final ModelosKenney kenney = new ModelosKenney();
    final PerspectiveCamera camera;
    final PointLightEx luzMotor = new PointLightEx();
    final PointLightEx luzOrbital = new PointLightEx();
    final PointLightEx luzPlataforma = new PointLightEx();
    final PointLightEx luzEfeito = new PointLightEx();

    private final ModelBatch pbrBatch;
    private final ModelBatch classicoBatch = new ModelBatch();
    private final ModelBatch sombraBatch;
    private final Environment ambientePbr = new Environment();
    private final Environment ambienteClassico = new Environment();
    private final DirectionalShadowLight sol;
    private final Cubemap mapaDifuso;
    private final Cubemap mapaEspecular;
    private final Texture brdf;
    private final PosProcessamento pos = new PosProcessamento();

    Mundo3D() {
        PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
        config.numBones = 16;
        config.numDirectionalLights = 2;
        config.numPointLights = 4;
        pbrBatch = new ModelBatch(PBRShaderProvider.createDefault(config));
        sombraBatch = new ModelBatch(PBRShaderProvider.createDefaultDepth(16));

        int mapa = ControleToque.telaDeToque() ? 1024 : 2048; // celular: mapa de sombras menor (desempenho)
        sol = new DirectionalShadowLight(mapa, mapa, 48f, 48f, 1f, 120f);
        sol.direction.set(-0.45f, -1f, -0.35f).nor();
        sol.baseColor.set(1f, 0.88f, 0.75f, 1f);
        sol.intensity = 2.6f;
        sol.updateColor();

        IBLBuilder ibl = IBLBuilder.createOutdoor(sol);
        ibl.nearGroundColor.set(0.3f, 0.14f, 0.09f, 1f);
        ibl.farGroundColor.set(0.2f, 0.09f, 0.06f, 1f);
        ibl.nearSkyColor.set(0.55f, 0.38f, 0.32f, 1f);
        ibl.farSkyColor.set(0.35f, 0.25f, 0.28f, 1f);
        mapaDifuso = ibl.buildIrradianceMap(256);
        mapaEspecular = ibl.buildRadianceMap(10);
        ibl.dispose();
        brdf = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        luzMotor.color.set(1f, 0.55f, 0.2f, 1f);
        luzMotor.range = 9f;
        luzOrbital.color.set(0.75f, 0.4f, 1f, 1f);
        luzOrbital.range = 7f;
        luzPlataforma.color.set(1f, 0.55f, 0.15f, 1f);
        luzPlataforma.range = 8f;
        luzPlataforma.position.set(0, 1.5f, 0);
        luzPlataforma.intensity = 6f;

        ambientePbr.add(sol);
        ambientePbr.add(luzMotor);
        ambientePbr.add(luzOrbital);
        ambientePbr.add(luzPlataforma);
        luzEfeito.range = 10f;
        luzEfeito.intensity = 0f;
        ambientePbr.add(luzEfeito);
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
    }

    /**
     * Desenha um quadro completo: sombras → cena PBR → efeitos → pós-processamento.
     * @param centroSombras ponto que a área de sombras acompanha (normalmente a nave)
     */
    void desenhar(Vector3 centroSombras, Array<ModelInstance> estaticos, Desenho pbr, Desenho efeitos) {
        sol.setCenter(centroSombras);
        sol.begin();
        sombraBatch.begin(sol.getCamera());
        sombraBatch.render(estaticos);
        pbr.desenhar(sombraBatch, null);
        sombraBatch.end();
        sol.end();

        pos.comecar();
        ScreenUtils.clear(CEU, true);
        pbrBatch.begin(camera);
        pbrBatch.render(estaticos, ambientePbr);
        pbr.desenhar(pbrBatch, ambientePbr);
        pbrBatch.end();
        classicoBatch.begin(camera);
        efeitos.desenhar(classicoBatch, ambienteClassico);
        classicoBatch.end();
        pos.terminar();
    }

    void redimensionar(int largura, int altura) {
        pos.redimensionar(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        camera.viewportWidth = largura;
        camera.viewportHeight = altura;
        camera.update();
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
