package br.edu.unifor.missaomarte.sobrevivencia;

/** Astronauta: assume o armamento — +20% de dano. */
public class Astronauta extends Passageiro {
    public static final float BONUS_DANO = 0.20f;

    public Astronauta(String nome, Vetor posicao) {
        super(nome, posicao);
    }

    @Override
    public void aplicarBonus(AtributosNave atributos) {
        atributos.aumentarDano(BONUS_DANO);
    }

    @Override
    public String descricaoBonus() {
        return "+20% dano";
    }
}
