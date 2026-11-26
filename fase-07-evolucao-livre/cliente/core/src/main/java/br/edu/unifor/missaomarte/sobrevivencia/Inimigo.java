package br.edu.unifor.missaomarte.sobrevivencia;

/** Inimigo que persegue a nave e causa dano ao encostar (no máximo a cada 0,5 s). */
public class Inimigo {
    static final float INTERVALO_DANO = 0.5f;

    private final TipoInimigo tipo;
    private Vetor posicao;
    private float vida;
    private float recargaDano;
    private ComportamentoChefe comportamento;

    public Inimigo(TipoInimigo tipo, Vetor posicao) {
        this.tipo = tipo;
        this.posicao = posicao;
        this.vida = tipo.vida();
    }

    /** Só o chefe tem padrão de ataque; os outros só perseguem. */
    void definirComportamento(ComportamentoChefe c) {
        this.comportamento = c;
    }

    public ComportamentoChefe comportamento() {
        return comportamento;
    }

    void esfriarDano(float delta) {
        recargaDano = Math.max(0, recargaDano - delta);
    }

    void perseguir(Vetor alvo, float delta) {
        Vetor dir = alvo.menos(posicao).normalizado();
        posicao = posicao.mais(dir.vezes(tipo.velocidade() * delta));
        recargaDano = Math.max(0, recargaDano - delta);
    }

    /** Afasta de outro inimigo para a horda não virar um único ponto. */
    void empurrar(Vetor deslocamento) {
        posicao = posicao.mais(deslocamento);
    }

    boolean podeCausarDano() {
        return recargaDano <= 0;
    }

    void reiniciarRecargaDano() {
        recargaDano = INTERVALO_DANO;
    }

    void sofrerDano(float dano) {
        vida -= dano;
    }

    public boolean morto() {
        return vida <= 0;
    }

    public TipoInimigo tipo() {
        return tipo;
    }

    public Vetor posicao() {
        return posicao;
    }

    public float vida() {
        return vida;
    }

    public float fracaoVida() {
        return Math.max(0, vida / tipo.vida());
    }
}
