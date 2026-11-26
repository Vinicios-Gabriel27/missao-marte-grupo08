package br.edu.unifor.missaomarte.dominio;

import java.util.List;
import java.util.Optional;

/** Eventos do turno, em ordem, e o passageiro embarcado (se houve). */
public record ResultadoTurno(List<Evento> eventos, Optional<Passageiro> embarcado) {
    public boolean contem(Evento evento) {
        return eventos.contains(evento);
    }
}
