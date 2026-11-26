package br.edu.unifor.missaomarte.online;

/** Retorno assíncrono de uma chamada ao servidor (entregue na thread do jogo). */
public interface Resposta<T> {
    void sucesso(T valor);

    void falha(String mensagem);
}
