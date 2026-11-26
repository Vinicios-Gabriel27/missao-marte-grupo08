package br.edu.unifor.missaomarte.sobrevivencia;

/**
 * Padrão de ataque do chefe (máquina de estados):
 * PERSEGUIR → MIRAR (para e "avisa") → INVESTIR (arrancada na direção travada) → ANEL
 * (dispara projéteis em círculo) → PERSEGUIR. Com menos da metade da vida, fica mais
 * rápido e dispara anéis maiores.
 */
public class ComportamentoChefe {
    public enum Estado { PERSEGUIR, MIRAR, INVESTIR, ANEL }

    static final float TEMPO_PERSEGUIR = 3.5f;
    static final float TEMPO_MIRAR = 1.0f;
    static final float TEMPO_INVESTIR = 0.7f;
    static final float TEMPO_ANEL = 0.6f;
    static final float VELOCIDADE_INVESTIDA = 13f;

    private Estado estado = Estado.PERSEGUIR;
    private float tempoNoEstado;
    private Vetor direcaoTravada = Vetor.ZERO;

    void atualizar(float delta, Inimigo chefe, PartidaSobrevivencia partida) {
        boolean furioso = chefe.fracaoVida() < 0.5f;
        float ritmo = furioso ? 1.35f : 1f;
        tempoNoEstado += delta * ritmo;
        Vetor alvo = partida.nave().posicao();
        switch (estado) {
            case PERSEGUIR -> {
                chefe.perseguir(alvo, delta);
                if (tempoNoEstado >= TEMPO_PERSEGUIR) {
                    trocar(Estado.MIRAR);
                    partida.registrar(Evento.CHEFE_MIRANDO);
                }
            }
            case MIRAR -> {
                direcaoTravada = alvo.menos(chefe.posicao()).normalizado();
                chefe.esfriarDano(delta);
                if (tempoNoEstado >= TEMPO_MIRAR) {
                    trocar(Estado.INVESTIR);
                    partida.registrar(Evento.CHEFE_INVESTIU);
                }
            }
            case INVESTIR -> {
                chefe.empurrar(direcaoTravada.vezes(VELOCIDADE_INVESTIDA * ritmo * delta));
                chefe.esfriarDano(delta);
                if (tempoNoEstado >= TEMPO_INVESTIR) {
                    trocar(Estado.ANEL);
                    disparar(chefe, partida, furioso ? 24 : 16);
                    if (furioso) {
                        disparar(chefe, partida, 12);
                    }
                    partida.registrar(Evento.CHEFE_ANEL);
                }
            }
            case ANEL -> {
                chefe.esfriarDano(delta);
                if (tempoNoEstado >= TEMPO_ANEL) {
                    trocar(Estado.PERSEGUIR);
                }
            }
            default -> { }
        }
    }

    private void disparar(Inimigo chefe, PartidaSobrevivencia partida, int quantidade) {
        float defasagem = partida.tempo() * 0.7f;
        for (int i = 0; i < quantidade; i++) {
            float a = defasagem + (float) (2 * Math.PI * i / quantidade);
            partida.dispararInimigo(new ProjetilInimigo(chefe.posicao(), Vetor.polar(a, 1f)));
        }
    }

    private void trocar(Estado novo) {
        estado = novo;
        tempoNoEstado = 0;
    }

    public Estado estado() {
        return estado;
    }

    /** Progresso (0..1) do estado atual — a tela usa para animar o aviso da investida. */
    public float progresso() {
        float total = switch (estado) {
            case PERSEGUIR -> TEMPO_PERSEGUIR;
            case MIRAR -> TEMPO_MIRAR;
            case INVESTIR -> TEMPO_INVESTIR;
            case ANEL -> TEMPO_ANEL;
        };
        return Math.min(1f, tempoNoEstado / total);
    }

    public Vetor direcaoTravada() {
        return direcaoTravada;
    }
}
