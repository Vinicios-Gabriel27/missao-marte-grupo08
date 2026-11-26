package br.edu.unifor.missaomarte.dominio;

/** Passageiro a resgatar. Cada subtipo define quanto vale (polimorfismo no lugar de if por tipo). */
public abstract class Passageiro extends EntidadeMapa {
    private final String nome;

    protected Passageiro(String nome, Posicao posicao) {
        super(posicao);
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public abstract String getTipo();

    public abstract int getPontuacao();
}
