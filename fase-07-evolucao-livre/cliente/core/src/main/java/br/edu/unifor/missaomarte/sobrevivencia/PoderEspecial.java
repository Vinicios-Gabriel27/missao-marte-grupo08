package br.edu.unifor.missaomarte.sobrevivencia;

/**
 * Habilidade ativa da nave, com recarga (ideia de The Deep Horde: "um poder usado no momento
 * certo vira a batalha"). Strategy: outra nave poderia ter outro poder sem mudar a Partida.
 */
public interface PoderEspecial {
    String nome();

    /** Segundos de recarga completa. */
    float recargaTotal();

    float recargaRestante();

    default boolean pronto() {
        return recargaRestante() <= 0;
    }

    /** Verdadeiro enquanto o poder controla o movimento da nave. */
    boolean emAcao();

    void ativar(PartidaSobrevivencia partida);

    void atualizar(float delta, PartidaSobrevivencia partida);
}
