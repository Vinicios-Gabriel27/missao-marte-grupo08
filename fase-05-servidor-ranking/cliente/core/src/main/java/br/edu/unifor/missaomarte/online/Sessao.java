package br.edu.unifor.missaomarte.online;

/** Jogador autenticado. O token fica só na memória (não é gravado em disco). */
public record Sessao(String nome, String token) {
}
