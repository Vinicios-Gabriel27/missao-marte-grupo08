package br.edu.unifor.missaomarte.dominio;

/** Obstáculo que se move aleatoriamente uma casa (ou fica parado) a cada turno. */
public class Inimigo extends EntidadeMapa {
    public Inimigo(Posicao posicao) {
        super(posicao);
    }

    /** Tenta andar; se sair do mapa, permanece onde está (regra do jogo original). */
    public void vagar(java.util.Random random, Limites limites) {
        int dx = random.nextInt(3) - 1;
        int dy = random.nextInt(3) - 1;
        Posicao destino = posicao.deslocar(dx, dy);
        if (limites.contem(destino)) {
            posicao = destino;
        }
    }
}
