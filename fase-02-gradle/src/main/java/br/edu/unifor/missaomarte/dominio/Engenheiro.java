package br.edu.unifor.missaomarte.dominio;

public class Engenheiro extends Passageiro {
    public Engenheiro(String nome, Posicao posicao) {
        super(nome, posicao);
    }

    @Override
    public String getTipo() {
        return "Engenheiro";
    }

    @Override
    public int getPontuacao() {
        return 15;
    }
}
