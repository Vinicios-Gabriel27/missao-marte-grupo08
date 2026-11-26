package br.edu.unifor.missaomarte.sobrevivencia;

/**
 * Números que upgrades e passageiros modificam. Ficam juntos para que um bônus
 * novo só precise mexer aqui, e não no laço do jogo.
 */
public class AtributosNave {
    private float multiplicadorDano = 1f;
    private float multiplicadorCadencia = 1f;
    private float velocidade = 7f;
    private float vidaMaxima;
    private float alcanceIma = 3f;
    private float multiplicadorXp = 1f;
    private float regeneracaoPorSegundo = 0f;

    public AtributosNave(float vidaMaxima) {
        this.vidaMaxima = vidaMaxima;
    }

    public void aumentarDano(float fracao) {
        multiplicadorDano += fracao;
    }

    public void aumentarCadencia(float fracao) {
        multiplicadorCadencia += fracao;
    }

    public void aumentarVelocidade(float fracao) {
        velocidade *= 1 + fracao;
    }

    public void aumentarVidaMaxima(float valor) {
        vidaMaxima += valor;
    }

    public void aumentarAlcanceIma(float fracao) {
        alcanceIma *= 1 + fracao;
    }

    public void aumentarXp(float fracao) {
        multiplicadorXp += fracao;
    }

    public void aumentarRegeneracao(float porSegundo) {
        regeneracaoPorSegundo += porSegundo;
    }

    public float multiplicadorDano() {
        return multiplicadorDano;
    }

    public float multiplicadorCadencia() {
        return multiplicadorCadencia;
    }

    public float velocidade() {
        return velocidade;
    }

    public float vidaMaxima() {
        return vidaMaxima;
    }

    public float alcanceIma() {
        return alcanceIma;
    }

    public float multiplicadorXp() {
        return multiplicadorXp;
    }

    public float regeneracaoPorSegundo() {
        return regeneracaoPorSegundo;
    }
}
