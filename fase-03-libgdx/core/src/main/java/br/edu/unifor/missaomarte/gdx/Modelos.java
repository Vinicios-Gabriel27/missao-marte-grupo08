package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * Fábrica dos modelos 3D low-poly, montados com formas simples (sem arquivos de modelo).
 * Convenção: a frente de cada modelo aponta para +Z.
 */
@SuppressWarnings("deprecation")
public class Modelos implements Disposable {
    private static final long ATRIBUTOS = Usage.Position | Usage.Normal;

    public final Model nave;
    public final Model perseguidor;
    public final Model rapido;
    public final Model tanque;
    public final Model chefe;
    public final Model projetil;
    public final Model cristal;
    public final Model professor;
    public final Model engenheiro;
    public final Model astronauta;
    public final Model plataforma;
    public final Model plataformaAberta;
    public final Model chao;
    public final Model rocha;
    public final Model cratera;
    public final Model muro;
    public final Model orbital;
    public final Model onda;
    public final Model seta;
    public final Model particula;
    public final Model montanha;
    private final Texture texturaChao;

    private final ModelBuilder mb = new ModelBuilder();

    public Modelos() {
        nave = criarNave();
        perseguidor = criarPerseguidor();
        rapido = criarRapido();
        tanque = criarTanque();
        chefe = criarChefe();
        projetil = mb.createSphere(0.22f, 0.22f, 0.9f, 6, 4, emissivo(new Color(0.4f, 1f, 1f, 1f)), ATRIBUTOS);
        cristal = mb.createSphere(0.45f, 0.7f, 0.45f, 4, 2, emissivo(new Color(0.3f, 1f, 0.45f, 1f)), ATRIBUTOS);
        professor = criarPassageiro(new Color(0.25f, 0.45f, 1f, 1f));
        engenheiro = criarPassageiro(new Color(1f, 0.55f, 0.1f, 1f));
        astronauta = criarPassageiro(new Color(0.95f, 0.95f, 0.95f, 1f));
        plataforma = criarPlataforma(false);
        plataformaAberta = criarPlataforma(true);
        texturaChao = TexturaMarte.criar(7L);
        TextureAttribute areia = TextureAttribute.createDiffuse(texturaChao);
        areia.scaleU = 14f;
        areia.scaleV = 14f;
        chao = mb.createRect(-0.5f, 0f, 0.5f, 0.5f, 0f, 0.5f, 0.5f, 0f, -0.5f, -0.5f, 0f, -0.5f, 0f, 1f, 0f,
                new Material(areia), ATRIBUTOS | Usage.TextureCoordinates);
        rocha = mb.createSphere(1f, 1f, 1f, 5, 3, difuso(new Color(0.42f, 0.2f, 0.13f, 1f)), ATRIBUTOS);
        cratera = mb.createCylinder(1f, 0.05f, 1f, 10, difuso(new Color(0.48f, 0.2f, 0.12f, 1f)), ATRIBUTOS);
        muro = mb.createBox(1f, 1f, 1f, difuso(new Color(0.3f, 0.14f, 0.1f, 1f)), ATRIBUTOS);
        orbital = mb.createSphere(0.9f, 0.9f, 0.9f, 8, 6, emissivo(new Color(0.75f, 0.4f, 1f, 1f)), ATRIBUTOS);
        Material anel = emissivo(new Color(0.4f, 0.8f, 1f, 1f));
        anel.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.5f));
        onda = mb.createCylinder(2f, 0.08f, 2f, 32, anel, ATRIBUTOS);
        seta = criarSeta();
        particula = mb.createBox(1f, 1f, 1f, emissivo(Color.WHITE), ATRIBUTOS);
        montanha = mb.createCone(1f, 1f, 1f, 6, difuso(new Color(0.5f, 0.22f, 0.14f, 1f)), ATRIBUTOS);
    }

    private Model criarNave() {
        mb.begin();
        Matrix4 t = new Matrix4();
        MeshPartBuilder corpo = mb.part("corpo", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(0.85f, 0.88f, 0.92f, 1f)));
        corpo.setVertexTransform(t.setToRotation(Vector3.X, 90));
        corpo.cone(0.9f, 1.9f, 0.6f, 8);
        MeshPartBuilder asas = mb.part("asas", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(0.85f, 0.15f, 0.12f, 1f)));
        asas.setVertexTransform(null);
        asas.box(0f, 0f, -0.35f, 2.3f, 0.12f, 0.7f);
        asas.box(0f, 0.35f, -0.65f, 0.12f, 0.6f, 0.5f);
        MeshPartBuilder cabine = mb.part("cabine", GL20.GL_TRIANGLES, ATRIBUTOS, emissivo(new Color(0.3f, 0.85f, 1f, 1f)));
        cabine.setVertexTransform(t.setToTranslation(0f, 0.22f, 0.15f));
        cabine.sphere(0.42f, 0.32f, 0.6f, 8, 5);
        MeshPartBuilder motor = mb.part("motor", GL20.GL_TRIANGLES, ATRIBUTOS, emissivo(new Color(1f, 0.55f, 0.15f, 1f)));
        motor.setVertexTransform(t.setToTranslation(0f, 0f, -0.95f).rotate(Vector3.X, 90));
        motor.cylinder(0.45f, 0.2f, 0.45f, 8);
        return mb.end();
    }

    private Model criarPerseguidor() {
        mb.begin();
        Matrix4 t = new Matrix4();
        MeshPartBuilder corpo = mb.part("corpo", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(0.85f, 0.12f, 0.18f, 1f)));
        corpo.setVertexTransform(t.setToTranslation(0f, 0.6f, 0f));
        corpo.sphere(1.2f, 1.1f, 1.2f, 6, 4);
        MeshPartBuilder chifres = mb.part("chifres", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(0.25f, 0.05f, 0.08f, 1f)));
        chifres.setVertexTransform(t.setToTranslation(0.3f, 1.25f, 0.1f).rotate(Vector3.Z, -20));
        chifres.cone(0.22f, 0.5f, 0.22f, 5);
        chifres.setVertexTransform(t.setToTranslation(-0.3f, 1.25f, 0.1f).rotate(Vector3.Z, 20));
        chifres.cone(0.22f, 0.5f, 0.22f, 5);
        MeshPartBuilder olhos = mb.part("olhos", GL20.GL_TRIANGLES, ATRIBUTOS, emissivo(new Color(1f, 0.9f, 0.2f, 1f)));
        olhos.setVertexTransform(null);
        olhos.box(0.22f, 0.75f, 0.55f, 0.16f, 0.16f, 0.1f);
        olhos.box(-0.22f, 0.75f, 0.55f, 0.16f, 0.16f, 0.1f);
        return mb.end();
    }

    private Model criarRapido() {
        mb.begin();
        Matrix4 t = new Matrix4();
        MeshPartBuilder corpo = mb.part("corpo", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(1f, 0.82f, 0.1f, 1f)));
        corpo.setVertexTransform(t.setToTranslation(0f, 0.45f, 0f).rotate(Vector3.X, 90));
        corpo.cone(0.8f, 1.3f, 0.5f, 5);
        MeshPartBuilder cauda = mb.part("cauda", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(0.15f, 0.12f, 0.1f, 1f)));
        cauda.setVertexTransform(null);
        cauda.box(0f, 0.45f, -0.6f, 0.9f, 0.08f, 0.3f);
        return mb.end();
    }

    private Model criarTanque() {
        mb.begin();
        MeshPartBuilder corpo = mb.part("corpo", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(0.45f, 0.2f, 0.65f, 1f)));
        corpo.setVertexTransform(null);
        corpo.box(0f, 0.8f, 0f, 1.8f, 1.4f, 1.8f);
        MeshPartBuilder placas = mb.part("placas", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(0.25f, 0.1f, 0.35f, 1f)));
        placas.setVertexTransform(null);
        placas.box(0f, 1.6f, 0f, 1.3f, 0.3f, 1.3f);
        placas.box(1f, 0.6f, 0f, 0.25f, 0.9f, 1.4f);
        placas.box(-1f, 0.6f, 0f, 0.25f, 0.9f, 1.4f);
        MeshPartBuilder olho = mb.part("olho", GL20.GL_TRIANGLES, ATRIBUTOS, emissivo(new Color(1f, 0.25f, 0.25f, 1f)));
        olho.setVertexTransform(null);
        olho.box(0f, 1f, 0.92f, 0.8f, 0.2f, 0.06f);
        return mb.end();
    }

    private Model criarChefe() {
        mb.begin();
        Matrix4 t = new Matrix4();
        MeshPartBuilder corpo = mb.part("corpo", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(0.45f, 0.04f, 0.06f, 1f)));
        corpo.setVertexTransform(t.setToTranslation(0f, 2f, 0f));
        corpo.sphere(4f, 3.6f, 4f, 10, 7);
        MeshPartBuilder espinhos = mb.part("espinhos", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(0.12f, 0.1f, 0.1f, 1f)));
        espinhos.setVertexTransform(null);
        for (int i = 0; i < 8; i++) {
            espinhos.setVertexTransform(t.setToTranslation(0f, 2f, 0f).rotate(Vector3.Y, i * 45f).rotate(Vector3.X, 70).translate(0f, 2.1f, 0f));
            espinhos.cone(0.6f, 1.4f, 0.6f, 5);
        }
        espinhos.setVertexTransform(t.setToTranslation(0f, 4f, 0f));
        espinhos.cone(0.8f, 1.6f, 0.8f, 5);
        MeshPartBuilder olhos = mb.part("olhos", GL20.GL_TRIANGLES, ATRIBUTOS, emissivo(new Color(1f, 0.85f, 0.1f, 1f)));
        olhos.setVertexTransform(null);
        olhos.box(0.7f, 2.5f, 1.85f, 0.5f, 0.35f, 0.2f);
        olhos.box(-0.7f, 2.5f, 1.85f, 0.5f, 0.35f, 0.2f);
        return mb.end();
    }

    /** Bonequinho com capacete na cor do tipo e um feixe de luz para ser visto de longe. */
    private Model criarPassageiro(Color corCapacete) {
        mb.begin();
        Matrix4 t = new Matrix4();
        MeshPartBuilder corpo = mb.part("corpo", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(0.9f, 0.9f, 0.88f, 1f)));
        corpo.setVertexTransform(t.setToTranslation(0f, 0.55f, 0f));
        corpo.cylinder(0.6f, 0.8f, 0.45f, 8);
        corpo.setVertexTransform(t.setToTranslation(0.18f, 0.12f, 0f));
        corpo.cylinder(0.2f, 0.3f, 0.2f, 6);
        corpo.setVertexTransform(t.setToTranslation(-0.18f, 0.12f, 0f));
        corpo.cylinder(0.2f, 0.3f, 0.2f, 6);
        MeshPartBuilder capacete = mb.part("capacete", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(corCapacete));
        capacete.setVertexTransform(t.setToTranslation(0f, 1.2f, 0f));
        capacete.sphere(0.62f, 0.62f, 0.62f, 8, 6);
        MeshPartBuilder visor = mb.part("visor", GL20.GL_TRIANGLES, ATRIBUTOS, emissivo(new Color(1f, 0.8f, 0.3f, 1f)));
        visor.setVertexTransform(null);
        visor.box(0f, 1.22f, 0.27f, 0.38f, 0.2f, 0.1f);
        Material feixe = emissivo(corCapacete);
        feixe.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.35f));
        MeshPartBuilder luz = mb.part("feixe", GL20.GL_TRIANGLES, ATRIBUTOS, feixe);
        luz.setVertexTransform(t.setToTranslation(0f, 4f, 0f));
        luz.cylinder(0.25f, 5f, 0.25f, 6);
        return mb.end();
    }

    private Model criarPlataforma(boolean aberta) {
        mb.begin();
        Matrix4 t = new Matrix4();
        MeshPartBuilder base = mb.part("base", GL20.GL_TRIANGLES, ATRIBUTOS, difuso(new Color(0.55f, 0.57f, 0.6f, 1f)));
        base.setVertexTransform(t.setToTranslation(0f, 0.1f, 0f));
        base.cylinder(3.6f, 0.2f, 3.6f, 12);
        Color cor = aberta ? new Color(0.2f, 1f, 0.35f, 1f) : new Color(1f, 0.5f, 0.1f, 1f);
        MeshPartBuilder anel = mb.part("anel", GL20.GL_TRIANGLES, ATRIBUTOS, emissivo(cor));
        anel.setVertexTransform(t.setToTranslation(0f, 0.22f, 0f));
        anel.cylinder(2.4f, 0.06f, 2.4f, 12);
        if (aberta) {
            Material feixe = emissivo(cor);
            feixe.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.3f));
            MeshPartBuilder luz = mb.part("feixe", GL20.GL_TRIANGLES, ATRIBUTOS, feixe);
            luz.setVertexTransform(t.setToTranslation(0f, 6f, 0f));
            luz.cylinder(2.2f, 12f, 2.2f, 12);
        }
        return mb.end();
    }

    private Model criarSeta() {
        mb.begin();
        Matrix4 t = new Matrix4();
        MeshPartBuilder p = mb.part("seta", GL20.GL_TRIANGLES, ATRIBUTOS, emissivo(new Color(1f, 0.9f, 0.2f, 1f)));
        p.setVertexTransform(t.setToRotation(Vector3.X, 90));
        p.cone(0.5f, 0.8f, 0.12f, 3);
        return mb.end();
    }

    private static Material difuso(Color cor) {
        return new Material(ColorAttribute.createDiffuse(cor));
    }

    /** Material que "brilha" (não depende da luz) — tiros, cristais, visores. */
    private static Material emissivo(Color cor) {
        return new Material(ColorAttribute.createDiffuse(cor), ColorAttribute.createEmissive(cor));
    }

    @Override
    public void dispose() {
        for (Model m : new Model[] {nave, perseguidor, rapido, tanque, chefe, projetil, cristal, professor,
                engenheiro, astronauta, plataforma, plataformaAberta, chao, rocha, cratera, muro, orbital, onda, seta,
                particula, montanha}) {
            m.dispose();
        }
        texturaChao.dispose();
    }
}
