package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import java.util.Random;

/** Gera por código a textura de areia marciana (sem arquivo de imagem). */
final class TexturaMarte {
    private TexturaMarte() {
    }

    static Texture criar(long semente) {
        int n = 256;
        Random r = new Random(semente);
        float[][] ruido = ruidoSuave(n, r);
        Pixmap p = new Pixmap(n, n, Pixmap.Format.RGBA8888);
        Color base = new Color(0.66f, 0.31f, 0.18f, 1f);
        Color escuro = new Color(0.45f, 0.19f, 0.11f, 1f);
        Color claro = new Color(0.8f, 0.45f, 0.28f, 1f);
        Color c = new Color();
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                float v = ruido[x][y];
                if (v < 0.5f) {
                    c.set(escuro).lerp(base, v * 2f);
                } else {
                    c.set(base).lerp(claro, (v - 0.5f) * 2f);
                }
                float grao = (r.nextFloat() - 0.5f) * 0.06f;
                c.add(grao, grao, grao, 0).clamp();
                p.setColor(c);
                p.drawPixel(x, y);
            }
        }
        // pedrinhas
        for (int i = 0; i < 260; i++) {
            float t = r.nextFloat();
            p.setColor(t < 0.5f ? new Color(0.35f, 0.15f, 0.09f, 1f) : new Color(0.85f, 0.55f, 0.38f, 1f));
            p.fillCircle(r.nextInt(n), r.nextInt(n), 1 + r.nextInt(2));
        }
        Texture tex = new Texture(p, true);
        tex.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        p.dispose();
        return tex;
    }

    /** Ruído de valor em várias escalas, repetível nas bordas (para ladrilhar sem emenda). */
    private static float[][] ruidoSuave(int n, Random r) {
        float[][] soma = new float[n][n];
        float peso = 1f;
        float total = 0f;
        for (int celulas = 4; celulas <= 32; celulas *= 2) {
            float[][] grade = new float[celulas][celulas];
            for (int i = 0; i < celulas; i++) {
                for (int j = 0; j < celulas; j++) {
                    grade[i][j] = r.nextFloat();
                }
            }
            for (int x = 0; x < n; x++) {
                for (int y = 0; y < n; y++) {
                    float gx = (float) x / n * celulas;
                    float gy = (float) y / n * celulas;
                    int x0 = (int) gx;
                    int y0 = (int) gy;
                    float fx = suavizar(gx - x0);
                    float fy = suavizar(gy - y0);
                    float a = grade[x0 % celulas][y0 % celulas];
                    float b = grade[(x0 + 1) % celulas][y0 % celulas];
                    float c = grade[x0 % celulas][(y0 + 1) % celulas];
                    float d = grade[(x0 + 1) % celulas][(y0 + 1) % celulas];
                    soma[x][y] += peso * lerp(lerp(a, b, fx), lerp(c, d, fx), fy);
                }
            }
            total += peso;
            peso *= 0.5f;
        }
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                soma[x][y] /= total;
            }
        }
        return soma;
    }

    private static float suavizar(float t) {
        return t * t * (3 - 2 * t);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
