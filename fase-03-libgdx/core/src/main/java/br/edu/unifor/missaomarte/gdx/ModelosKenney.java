package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/**
 * Modelos 3D dos pacotes Kenney (CC0): Space Kit e Mini Characters, carregados com a gdx-gltf.
 * Os personagens têm animações (andar, correr, parado...).
 */
class ModelosKenney implements Disposable {
    private static final String PERSONAGENS = "modelos/personagens/";
    private static final String ESPACO = "modelos/espaco/";
    private static final int VARIANTES_HORDA = 6;

    private final Array<SceneAsset> carregados = new Array<>();

    final Modelo3D nave;
    /** Na ordem de TipoInimigo: PERSEGUIDOR, RAPIDO, TANQUE, CHEFE. */
    final Modelo3D[] inimigos;
    final Modelo3D professor;
    final Modelo3D engenheiro;
    final Modelo3D astronauta;
    final Modelo3D plataforma;
    final Array<Model> rochas = new Array<>();
    final Array<Model> crateras = new Array<>();
    final Array<Model> estruturas = new Array<>();

    ModelosKenney() {
        nave = new Modelo3D(carregar(ESPACO + "craft_speederA.glb"), 2.6f, false, 180f, 1, null);
        inimigos = new Modelo3D[] {
            new Modelo3D(carregar(PERSONAGENS + "character-alien.glb"), 1.5f, true, 0f, VARIANTES_HORDA, "walk"),
            new Modelo3D(carregar(PERSONAGENS + "character-zombie.glb"), 1.3f, true, 0f, VARIANTES_HORDA, "sprint"),
            new Modelo3D(carregar(PERSONAGENS + "character-robot.glb"), 2.7f, true, 0f, VARIANTES_HORDA, "walk"),
            new Modelo3D(carregar(PERSONAGENS + "character-knight.glb"), 6.5f, true, 0f, 1, "walk"),
        };
        professor = new Modelo3D(carregar(PERSONAGENS + "character-male-b.glb"), 1.45f, true, 0f, 3, "emote-yes");
        engenheiro = new Modelo3D(carregar(PERSONAGENS + "character-worker.glb"), 1.45f, true, 0f, 3, "emote-yes");
        astronauta = new Modelo3D(carregar(ESPACO + "astronautA.glb"), 1.5f, true, 0f, 3, null);
        plataforma = new Modelo3D(carregar(ESPACO + "platform_large.glb"), 4.2f, false, 0f, 1, null);
        for (String n : new String[] {"rock", "rock_largeA", "rock_largeB", "rocks_smallA", "rocks_smallB",
                "meteor_detailed", "rock_crystals", "rock_crystalsLargeA"}) {
            rochas.add(carregar(ESPACO + n + ".glb"));
        }
        crateras.add(carregar(ESPACO + "crater.glb"));
        crateras.add(carregar(ESPACO + "craterLarge.glb"));
        for (String n : new String[] {"hangar_roundA", "satelliteDish_large", "turret_double", "rover",
                "machine_generatorLarge", "structure_detailed"}) {
            estruturas.add(carregar(ESPACO + n + ".glb"));
        }
    }

    private Model carregar(String caminho) {
        SceneAsset asset = new GLBLoader().load(Gdx.files.internal(caminho));
        carregados.add(asset);
        return asset.scene.model;
    }

    void atualizar(float delta) {
        nave.atualizar(delta);
        for (Modelo3D m : inimigos) {
            m.atualizar(delta);
        }
        professor.atualizar(delta);
        engenheiro.atualizar(delta);
        astronauta.atualizar(delta);
    }

    @Override
    public void dispose() {
        for (SceneAsset a : carregados) {
            a.dispose();
        }
    }
}
