package br.edu.unifor.missaomarte.sobrevivencia;

/** Engenheiro: conserta a nave em voo — +1 de vida por segundo. */
public class Engenheiro extends Passageiro {
    public static final float REGENERACAO = 1f;

    public Engenheiro(String nome, Vetor posicao) {
        super(nome, posicao);
    }

    @Override
    public void aplicarBonus(AtributosNave atributos) {
        atributos.aumentarRegeneracao(REGENERACAO);
    }

    @Override
    public String descricaoBonus() {
        return "+1 vida/s";
    }
}
