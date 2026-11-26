package br.edu.unifor.missaomarte.servidor.api;

import br.edu.unifor.missaomarte.servidor.api.Dtos.JogadorResposta;
import br.edu.unifor.missaomarte.servidor.api.Dtos.PartidaResposta;
import br.edu.unifor.missaomarte.servidor.dominio.Jogador;
import br.edu.unifor.missaomarte.servidor.dominio.PartidaRegistrada;

/** Converte entidades em DTOs (as entidades não aparecem na API). */
final class Mapeador {
    private Mapeador() {
    }

    static JogadorResposta jogador(Jogador j) {
        return new JogadorResposta(j.getId(), j.getNome(), j.getCriadoEm());
    }

    static PartidaResposta partida(PartidaRegistrada p) {
        return new PartidaResposta(p.getId(), p.getJogador().getNome(), p.getPontuacao(), p.getDificuldade(),
                p.getTempoSegundos(), p.getNivel(), p.getAbates(), p.getPassageiros(), p.isVenceu(), p.getRegistradaEm());
    }
}
