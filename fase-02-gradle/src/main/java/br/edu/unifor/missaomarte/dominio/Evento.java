package br.edu.unifor.missaomarte.dominio;

/**
 * O que aconteceu em um turno. O domínio só informa fatos; cada interface
 * (console, libGDX, Android) decide como mostrar: texto, som, animação...
 */
public enum Evento {
    NAVE_MOVEU,
    NAVE_BLOQUEADA_NO_LIMITE,
    PASSAGEIRO_EMBARCADO,
    NENHUM_PASSAGEIRO_AQUI,
    NAVE_CHEIA,
    COLISAO,
    NAVE_DESTRUIDA,
    COMBUSTIVEL_ESGOTADO,
    RETORNE_A_PLATAFORMA,
    MISSAO_CUMPRIDA,
    MISSAO_ABORTADA
}
