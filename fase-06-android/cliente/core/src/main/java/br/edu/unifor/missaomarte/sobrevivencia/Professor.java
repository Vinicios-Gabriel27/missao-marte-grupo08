package br.edu.unifor.missaomarte.sobrevivencia;

/** Professor: ensina a aprender mais rápido — +25% de XP. */
public class Professor extends Passageiro {
    public static final float BONUS_XP = 0.25f;

    public Professor(String nome, Vetor posicao) {
        super(nome, posicao);
    }

    @Override
    public void aplicarBonus(AtributosNave atributos) {
        atributos.aumentarXp(BONUS_XP);
    }

    @Override
    public String descricaoBonus() {
        return "+25% XP";
    }
}
