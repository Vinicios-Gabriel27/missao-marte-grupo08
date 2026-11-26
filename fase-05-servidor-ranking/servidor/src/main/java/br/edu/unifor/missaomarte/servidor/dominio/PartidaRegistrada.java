package br.edu.unifor.missaomarte.servidor.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** Resultado de uma partida enviado pelo cliente autenticado. */
@Entity
@Table(name = "partida", indexes = @Index(name = "idx_partida_pontuacao", columnList = "pontuacao"))
public class PartidaRegistrada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jogador_id")
    private Jogador jogador;

    private int pontuacao;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private NivelDificuldade dificuldade;

    @Column(name = "tempo_segundos")
    private int tempoSegundos;

    private int nivel;
    private int abates;
    private int passageiros;
    private boolean venceu;

    @Column(name = "registrada_em", nullable = false)
    private Instant registradaEm;

    protected PartidaRegistrada() {
        // exigido pela JPA
    }

    public PartidaRegistrada(Jogador jogador, int pontuacao, NivelDificuldade dificuldade, int tempoSegundos, int nivel,
                             int abates, int passageiros, boolean venceu) {
        this.jogador = jogador;
        this.pontuacao = pontuacao;
        this.dificuldade = dificuldade;
        this.tempoSegundos = tempoSegundos;
        this.nivel = nivel;
        this.abates = abates;
        this.passageiros = passageiros;
        this.venceu = venceu;
        this.registradaEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Jogador getJogador() {
        return jogador;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public NivelDificuldade getDificuldade() {
        return dificuldade;
    }

    public int getTempoSegundos() {
        return tempoSegundos;
    }

    public int getNivel() {
        return nivel;
    }

    public int getAbates() {
        return abates;
    }

    public int getPassageiros() {
        return passageiros;
    }

    public boolean isVenceu() {
        return venceu;
    }

    public Instant getRegistradaEm() {
        return registradaEm;
    }
}
