package br.edu.unifor.missaomarte.dominio;

public class Professor extends Passageiro {
    public Professor(String nome, Posicao posicao) {
        super(nome, posicao);
    }

    @Override
    public String getTipo() {
        return "Professor";
    }

    @Override
    public int getPontuacao() {
        return 10;
    }
}
