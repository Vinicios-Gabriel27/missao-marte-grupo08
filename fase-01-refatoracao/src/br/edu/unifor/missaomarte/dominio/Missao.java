package br.edu.unifor.missaomarte.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Estado do mapa: nave, passageiros ainda na superfície e obstáculos.
 * Responde perguntas sobre o mapa porque é quem tem os dados (Information Expert).
 */
public class Missao {
    private final Nave nave;
    private final Limites limites;
    private final List<Passageiro> passageiros = new ArrayList<>();
    private final List<Asteroide> asteroides = new ArrayList<>();
    private final List<Inimigo> inimigos = new ArrayList<>();

    public Missao(Nave nave, Limites limites) {
        this.nave = nave;
        this.limites = limites;
    }

    public void adicionar(Passageiro p) {
        passageiros.add(p);
    }

    public void adicionar(Asteroide a) {
        asteroides.add(a);
    }

    public void adicionar(Inimigo i) {
        inimigos.add(i);
    }

    public Optional<Passageiro> passageiroSobANave() {
        return passageiros.stream().filter(p -> p.estaEm(nave.getPosicao())).findFirst();
    }

    /** Move o passageiro da superfície para dentro da nave. */
    void embarcar(Passageiro passageiro) {
        nave.embarcar(passageiro);
        passageiros.remove(passageiro);
    }

    void moverInimigos(java.util.Random random) {
        for (Inimigo inimigo : inimigos) {
            inimigo.vagar(random, limites);
        }
    }

    public boolean naveColidiu() {
        Posicao p = nave.getPosicao();
        return asteroides.stream().anyMatch(a -> a.estaEm(p)) || inimigos.stream().anyMatch(i -> i.estaEm(p));
    }

    public boolean todosEmbarcados() {
        return passageiros.isEmpty();
    }

    public boolean naveNaPlataforma() {
        return nave.estaEm(Posicao.PLATAFORMA);
    }

    /** Uma célula está ocupada se for a plataforma ou tiver qualquer entidade. */
    public boolean ocupada(Posicao p) {
        return p.equals(Posicao.PLATAFORMA) || nave.estaEm(p)
                || passageiros.stream().anyMatch(e -> e.estaEm(p))
                || asteroides.stream().anyMatch(e -> e.estaEm(p))
                || inimigos.stream().anyMatch(e -> e.estaEm(p));
    }

    public Nave getNave() {
        return nave;
    }

    public Limites getLimites() {
        return limites;
    }

    public List<Passageiro> getPassageiros() {
        return Collections.unmodifiableList(passageiros);
    }

    public List<Asteroide> getAsteroides() {
        return Collections.unmodifiableList(asteroides);
    }

    public List<Inimigo> getInimigos() {
        return Collections.unmodifiableList(inimigos);
    }
}
