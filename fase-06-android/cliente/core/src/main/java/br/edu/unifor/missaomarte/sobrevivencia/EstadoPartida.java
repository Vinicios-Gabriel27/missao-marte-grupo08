package br.edu.unifor.missaomarte.sobrevivencia;

public enum EstadoPartida {
    EM_ANDAMENTO, ESCOLHENDO_UPGRADE, VITORIA, DERROTA;

    public boolean encerrada() {
        return this == VITORIA || this == DERROTA;
    }
}
