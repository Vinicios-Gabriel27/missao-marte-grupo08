package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Pós-processamento: a cena é desenhada numa textura e depois recebe bloom (brilho que
 * "vaza" das partes claras), tratamento de cor e vinheta. Os shaders ficam no código para
 * funcionarem igual no desktop e no Android.
 */
class PosProcessamento implements Disposable {
    private static final String VERTICE = ""
            + "attribute vec4 a_position;\n"
            + "attribute vec4 a_color;\n"
            + "attribute vec2 a_texCoord0;\n"
            + "uniform mat4 u_projTrans;\n"
            + "varying vec2 v_uv;\n"
            + "void main() {\n"
            + "  v_uv = a_texCoord0;\n"
            + "  gl_Position = u_projTrans * a_position;\n"
            + "}\n";

    private static final String CABECALHO = "#ifdef GL_ES\nprecision mediump float;\n#endif\nvarying vec2 v_uv;\nuniform sampler2D u_texture;\n";

    /** Mantém só o que é mais claro que o limiar. */
    private static final String CLARIDADE = CABECALHO
            + "uniform float u_limiar;\n"
            + "void main() {\n"
            + "  vec3 c = texture2D(u_texture, v_uv).rgb;\n"
            + "  float l = max(max(c.r, c.g), c.b);\n"
            + "  float k = smoothstep(u_limiar, u_limiar + 0.25, l);\n"
            + "  gl_FragColor = vec4(c * k, 1.0);\n"
            + "}\n";

    /** Desfoque gaussiano em uma direção (rodado na horizontal e na vertical). */
    private static final String DESFOQUE = CABECALHO
            + "uniform vec2 u_passo;\n"
            + "void main() {\n"
            + "  vec3 s = texture2D(u_texture, v_uv).rgb * 0.2270270270;\n"
            + "  s += texture2D(u_texture, v_uv + u_passo * 1.3846153846).rgb * 0.3162162162;\n"
            + "  s += texture2D(u_texture, v_uv - u_passo * 1.3846153846).rgb * 0.3162162162;\n"
            + "  s += texture2D(u_texture, v_uv + u_passo * 3.2307692308).rgb * 0.0702702703;\n"
            + "  s += texture2D(u_texture, v_uv - u_passo * 3.2307692308).rgb * 0.0702702703;\n"
            + "  gl_FragColor = vec4(s, 1.0);\n"
            + "}\n";

    /** Junta cena + bloom, ajusta contraste/saturação/temperatura e escurece as bordas. */
    private static final String COMPOSICAO = CABECALHO
            + "uniform sampler2D u_bloom;\n"
            + "uniform float u_intensidade;\n"
            + "void main() {\n"
            + "  vec3 cena = texture2D(u_texture, v_uv).rgb;\n"
            + "  vec3 brilho = texture2D(u_bloom, v_uv).rgb;\n"
            + "  vec3 c = cena + brilho * u_intensidade;\n"
            + "  c = (c - 0.5) * 1.1 + 0.5;\n"                             // contraste
            + "  float cinza = dot(c, vec3(0.299, 0.587, 0.114));\n"
            + "  c = mix(vec3(cinza), c, 1.18);\n"                          // saturação
            + "  c *= vec3(1.04, 1.0, 0.95);\n"                             // tom quente
            + "  vec2 d = v_uv - 0.5;\n"
            + "  c *= 1.0 - dot(d, d) * 0.75;\n"                            // vinheta
            + "  gl_FragColor = vec4(clamp(c, 0.0, 1.0), 1.0);\n"
            + "}\n";

    private final SpriteBatch batch = new SpriteBatch(4);
    private final Matrix4 projecao = new Matrix4();
    private final ShaderProgram claridade = compilar(CLARIDADE);
    private final ShaderProgram desfoque = compilar(DESFOQUE);
    private final ShaderProgram composicao = compilar(COMPOSICAO);
    private FrameBuffer cena;
    private FrameBuffer brilhoA;
    private FrameBuffer brilhoB;

    PosProcessamento() {
        redimensionar(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
    }

    private static ShaderProgram compilar(String fragmento) {
        ShaderProgram s = new ShaderProgram(VERTICE, fragmento);
        if (!s.isCompiled()) {
            throw new GdxRuntimeException("Shader de pós-processamento: " + s.getLog());
        }
        return s;
    }

    void redimensionar(int largura, int altura) {
        liberarBuffers();
        largura = Math.max(1, largura);
        altura = Math.max(1, altura);
        cena = new FrameBuffer(Pixmap.Format.RGBA8888, largura, altura, true);
        brilhoA = new FrameBuffer(Pixmap.Format.RGBA8888, Math.max(1, largura / 4), Math.max(1, altura / 4), false);
        brilhoB = new FrameBuffer(Pixmap.Format.RGBA8888, Math.max(1, largura / 4), Math.max(1, altura / 4), false);
        for (FrameBuffer f : new FrameBuffer[] {cena, brilhoA, brilhoB}) {
            f.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
    }

    /** Tudo o que for desenhado entre {@link #comecar()} e {@link #terminar()} recebe os efeitos. */
    void comecar() {
        cena.begin();
    }

    void terminar() {
        cena.end();
        passar(cena, brilhoA, claridade, () -> claridade.setUniformf("u_limiar", 0.72f));
        for (int i = 0; i < 2; i++) {
            passar(brilhoA, brilhoB, desfoque, () -> desfoque.setUniformf("u_passo", 1f / brilhoA.getWidth(), 0f));
            passar(brilhoB, brilhoA, desfoque, () -> desfoque.setUniformf("u_passo", 0f, 1f / brilhoA.getHeight()));
        }
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        brilhoA.getColorBufferTexture().bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        desenhar(cena.getColorBufferTexture(), composicao, Gdx.graphics.getBackBufferWidth(),
                Gdx.graphics.getBackBufferHeight(), () -> {
                    composicao.setUniformi("u_bloom", 1);
                    composicao.setUniformf("u_intensidade", 1.15f);
                });
    }

    private void passar(FrameBuffer origem, FrameBuffer destino, ShaderProgram shader, Runnable uniformes) {
        destino.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        desenhar(origem.getColorBufferTexture(), shader, destino.getWidth(), destino.getHeight(), uniformes);
        destino.end();
    }

    private void desenhar(Texture textura, ShaderProgram shader, int largura, int altura, Runnable uniformes) {
        projecao.setToOrtho2D(0, 0, largura, altura);
        batch.setProjectionMatrix(projecao);
        batch.setShader(shader);
        batch.disableBlending();
        batch.begin();
        uniformes.run();
        batch.draw(textura, 0, 0, largura, altura, 0, 0, textura.getWidth(), textura.getHeight(), false, true);
        batch.end();
        batch.setShader(null);
    }

    private void liberarBuffers() {
        for (FrameBuffer f : new FrameBuffer[] {cena, brilhoA, brilhoB}) {
            if (f != null) {
                f.dispose();
            }
        }
    }

    @Override
    public void dispose() {
        liberarBuffers();
        batch.dispose();
        claridade.dispose();
        desfoque.dispose();
        composicao.dispose();
    }
}
