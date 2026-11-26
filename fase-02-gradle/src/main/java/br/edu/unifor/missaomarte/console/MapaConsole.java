package br.edu.unifor.missaomarte.console;

import br.edu.unifor.missaomarte.dominio.Asteroide;
import br.edu.unifor.missaomarte.dominio.Astronauta;
import br.edu.unifor.missaomarte.dominio.Engenheiro;
import br.edu.unifor.missaomarte.dominio.Inimigo;
import br.edu.unifor.missaomarte.dominio.Limites;
import br.edu.unifor.missaomarte.dominio.Missao;
import br.edu.unifor.missaomarte.dominio.Partida;
import br.edu.unifor.missaomarte.dominio.Passageiro;
import br.edu.unifor.missaomarte.dominio.Posicao;
import br.edu.unifor.missaomarte.dominio.Professor;
import java.io.PrintStream;

/** Desenha a missão como texto. Os símbolos são decisão de apresentação, por isso saíram do domínio. */
public class MapaConsole {
    private final PrintStream out;

    public MapaConsole(PrintStream out) {
        this.out = out;
    }

    public void desenhar(Partida partida, String piloto) {
        Missao m = partida.getMissao();
        Limites l = m.getLimites();
        out.println();
        out.printf("Mapa da Missão | Pontos: %d | Piloto: %s%n", partida.getPontuacao(), piloto);
        out.print("    ");
        for (int x = l.minX(); x <= l.maxX(); x++) {
            out.printf(" %2d", x);
        }
        out.println();
        out.print("    ");
        for (int x = l.minX(); x <= l.maxX(); x++) {
            out.print(" __");
        }
        out.println();
        for (int y = l.maxY(); y >= l.minY(); y--) {
            out.printf("%3d|", y);
            for (int x = l.minX(); x <= l.maxX(); x++) {
                out.printf(" %2c", simbolo(m, new Posicao(x, y)));
            }
            out.println();
        }
        out.println("Legenda: @=Nave, L=Plataforma, P=Professor, E=Engenheiro, T=Astronauta, #=Asteroide, X=Inimigo, .=Vazio");
        out.println("Comandos: w/s/a/d (mover), c (embarcar), q (sair)");
        out.println("Passageiros na superfície marciana:");
        for (Passageiro p : m.getPassageiros()) {
            out.printf(" - %s (%s) em (%d,%d)%n", p.getNome(), p.getTipo(), p.getPosicao().x(), p.getPosicao().y());
        }
    }

    private char simbolo(Missao m, Posicao p) {
        if (m.getNave().estaEm(p)) {
            return '@';
        }
        for (Passageiro passageiro : m.getPassageiros()) {
            if (passageiro.estaEm(p)) {
                return simboloPassageiro(passageiro);
            }
        }
        for (Asteroide a : m.getAsteroides()) {
            if (a.estaEm(p)) {
                return '#';
            }
        }
        for (Inimigo i : m.getInimigos()) {
            if (i.estaEm(p)) {
                return 'X';
            }
        }
        return p.equals(Posicao.PLATAFORMA) ? 'L' : '.';
    }

    private char simboloPassageiro(Passageiro p) {
        if (p instanceof Professor) {
            return 'P';
        }
        if (p instanceof Engenheiro) {
            return 'E';
        }
        if (p instanceof Astronauta) {
            return 'T';
        }
        return '?';
    }
}
