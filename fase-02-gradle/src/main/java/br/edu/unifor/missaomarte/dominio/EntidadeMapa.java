package br.edu.unifor.missaomarte.dominio;

/** Tudo que ocupa uma célula do mapa. Não sabe como é desenhado (isso é da interface). */
public abstract class EntidadeMapa {
    protected Posicao posicao;

    protected EntidadeMapa(Posicao posicao) {
        this.posicao = posicao;
    }

    public Posicao getPosicao() {
        return posicao;
    }

    public boolean estaEm(Posicao outra) {
        return posicao.equals(outra);
    }
}
