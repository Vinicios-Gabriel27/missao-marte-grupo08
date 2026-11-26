package br.edu.unifor.missaomarte.sobrevivencia;

/**
 * Passageiro a resgatar. Cada tipo aplica um bônus diferente na nave (polimorfismo):
 * um passageiro novo é só uma nova subclasse, sem if/switch por tipo no jogo (OCP).
 */
public abstract class Passageiro {
    public static final float RAIO = 0.8f;

    private final String nome;
    private final Vetor posicao;

    protected Passageiro(String nome, Vetor posicao) {
        this.nome = nome;
        this.posicao = posicao;
    }

    public abstract void aplicarBonus(AtributosNave atributos);

    public abstract String descricaoBonus();

    public String nome() {
        return nome;
    }

    public Vetor posicao() {
        return posicao;
    }
}
