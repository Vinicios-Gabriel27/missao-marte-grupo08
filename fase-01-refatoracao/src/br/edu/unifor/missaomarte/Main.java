package br.edu.unifor.missaomarte;

import br.edu.unifor.missaomarte.aplicacao.ServicoRanking;
import br.edu.unifor.missaomarte.console.JogoConsole;
import br.edu.unifor.missaomarte.infraestrutura.RepositorioRankingArquivo;
import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;

/** Composition root: o único lugar que escolhe as implementações concretas. */
public class Main {
    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("                    MISSÃO MARTE UNIFOR                          ");
        System.out.println("================================================================");
        System.out.println("  Pilote sua nave, salve os passageiros e desvie dos perigos!   ");
        System.out.println("================================================================");

        ServicoRanking ranking = new ServicoRanking(new RepositorioRankingArquivo(Path.of("ranking.txt")));
        try (Scanner scanner = new Scanner(System.in)) {
            new JogoConsole(scanner, System.out, ranking, new Random()).executar();
        }
    }
}
