package br.edu.unifor.missaomarte.dominio;

public enum EstadoPartida {
    EM_ANDAMENTO, VITORIA, DESTRUIDA, SEM_COMBUSTIVEL, ABORTADA;

    public boolean encerrada() {
        return this != EM_ANDAMENTO;
    }
}
