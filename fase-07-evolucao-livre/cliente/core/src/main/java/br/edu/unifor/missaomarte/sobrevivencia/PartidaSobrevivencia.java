package br.edu.unifor.missaomarte.sobrevivencia;

import br.edu.unifor.missaomarte.sobrevivencia.armas.Arma;
import br.edu.unifor.missaomarte.sobrevivencia.armas.TiroAutomatico;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.SorteadorUpgrades;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.Evolucao;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.OpcaoUpgrade;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeAtributo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Regras do modo sobrevivência, sem libGDX. A tela chama {@link #atualizar} a cada
 * quadro com o tempo decorrido e a direção do controle, e desenha o estado resultante.
 *
 * Vitória: sobreviver {@value #TEMPO_MINIMO} s e resgatar todos os passageiros abre a
 * plataforma (0,0), guardada pelo chefe e por uma horda; pousar nela vence. Vida 0 = derrota.
 */
public class PartidaSobrevivencia implements CampoDeBatalha {
    public static final float TEMPO_MINIMO = 300f;
    public static final float RAIO_PLATAFORMA = 1.8f;
    static final int MAXIMO_INIMIGOS = 250;
    static final int HORDA_FINAL = 24;

    private final Dificuldade dificuldade;
    private final Arena arena;
    private final Random random;
    private final GeradorOndas ondas;
    private final CronogramaPassageiros cronograma;
    private final SorteadorUpgrades sorteador;

    private final Nave nave;
    private final List<Arma> arsenal = new ArrayList<>();
    private final List<Inimigo> inimigos = new ArrayList<>();
    private final List<Projetil> projeteis = new ArrayList<>();
    private final List<CristalXp> cristais = new ArrayList<>();
    private final List<Passageiro> passageirosNoMapa = new ArrayList<>();
    private final List<Evento> eventosDoQuadro = new ArrayList<>();
    /** Eventos gerados entre quadros (ex.: poder ativado por toque); entram no próximo quadro. */
    private final List<Evento> eventosPendentes = new ArrayList<>();
    private boolean atualizando;

    private EstadoPartida estado = EstadoPartida.EM_ANDAMENTO;
    private float tempo;
    private int nivel = 1;
    private float xp;
    private int niveisPendentes;
    private int inimigosDerrotados;
    private boolean plataformaAberta;
    private List<OpcaoUpgrade> opcoesUpgrade = List.of();
    private final List<ProjetilInimigo> projeteisInimigos = new ArrayList<>();
    private final PoderEspecial poder = new Investida();
    private final Map<UpgradeAtributo, Integer> niveisPassivas = new EnumMap<>(UpgradeAtributo.class);
    private final List<Evolucao> evolucoes = new ArrayList<>();

    public PartidaSobrevivencia(Dificuldade dificuldade, Random random) {
        this(dificuldade, Arena.PADRAO, random, GeradorOndas.padrao(random, dificuldade.multiplicadorInimigos()),
                CronogramaPassageiros.padrao(random));
    }

    public PartidaSobrevivencia(Dificuldade dificuldade, Arena arena, Random random,
                                GeradorOndas ondas, CronogramaPassageiros cronograma) {
        this.dificuldade = dificuldade;
        this.arena = arena;
        this.random = random;
        this.ondas = ondas;
        this.cronograma = cronograma;
        this.sorteador = new SorteadorUpgrades(random);
        this.nave = new Nave(dificuldade.vidaInicial());
        this.arsenal.add(new TiroAutomatico());
    }

    /** Avança o jogo. Retorna os eventos do quadro. Pausado enquanto escolhe upgrade. */
    public List<Evento> atualizar(float delta, Vetor entrada) {
        eventosDoQuadro.clear();
        eventosDoQuadro.addAll(eventosPendentes);
        eventosPendentes.clear();
        if (estado != EstadoPartida.EM_ANDAMENTO) {
            return List.of();
        }
        atualizando = true;
        tempo += delta;
        nave.passarTempo(delta);
        poder.atualizar(delta, this);
        if (!poder.emAcao()) {
            nave.mover(entrada, delta, arena);
        }
        nave.curar(nave.atributos().regeneracaoPorSegundo() * delta);

        surgirPassageiros();
        surgirInimigos(delta);
        for (Arma arma : List.copyOf(arsenal)) {
            arma.atualizar(delta, this);
        }
        moverProjeteis(delta);
        moverInimigos(delta);
        moverProjeteisInimigos(delta);
        recolherMortos();
        coletarCristais(delta);
        resgatarPassageiros();
        verificarPlataforma();
        verificarFim();
        if (!estado.encerrada() && niveisPendentes > 0) {
            abrirEscolhaDeUpgrade();
        }
        atualizando = false;
        return List.copyOf(eventosDoQuadro);
    }

    public void escolherUpgrade(int indice) {
        if (estado != EstadoPartida.ESCOLHENDO_UPGRADE) {
            throw new IllegalStateException("Nenhum upgrade para escolher");
        }
        opcoesUpgrade.get(indice).aplicar(this);
        niveisPendentes--;
        if (niveisPendentes > 0) {
            opcoesUpgrade = sorteador.sortear(this, 3);
        } else {
            opcoesUpgrade = List.of();
            estado = EstadoPartida.EM_ANDAMENTO;
        }
    }

    // ---- etapas do quadro ----

    private void surgirPassageiros() {
        for (Passageiro p : cronograma.gerar(tempo, nave.posicao(), arena)) {
            passageirosNoMapa.add(p);
            registrar(Evento.PASSAGEIRO_APARECEU);
        }
    }

    private void surgirInimigos(float delta) {
        for (Inimigo i : ondas.gerar(tempo, delta, nave.posicao(), arena)) {
            if (inimigos.size() < MAXIMO_INIMIGOS) {
                inimigos.add(i);
            }
        }
    }

    private void moverProjeteis(float delta) {
        Iterator<Projetil> it = projeteis.iterator();
        while (it.hasNext()) {
            Projetil p = it.next();
            p.avancar(delta);
            for (Inimigo i : inimigos) {
                if (!i.morto() && p.posicao().distancia(i.posicao()) <= Projetil.RAIO + i.tipo().raio()) {
                    causarDano(i, p.dano());
                    p.consumir();
                    break;
                }
            }
            if (!p.ativo() || !arena.contem(p.posicao())) {
                it.remove();
            }
        }
    }

    private void moverInimigos(float delta) {
        for (Inimigo i : List.copyOf(inimigos)) {
            if (i.comportamento() != null) {
                i.comportamento().atualizar(delta, i, this);
            } else {
                i.perseguir(nave.posicao(), delta);
            }
        }
        separarInimigos();
        for (Inimigo i : inimigos) {
            if (!i.morto() && i.podeCausarDano() && !nave.invulneravel()
                    && i.posicao().distancia(nave.posicao()) <= Nave.RAIO + i.tipo().raio()) {
                nave.sofrerDano(i.tipo().danoContato());
                i.reiniciarRecargaDano();
                registrar(Evento.NAVE_ATINGIDA);
            }
        }
    }

    private void moverProjeteisInimigos(float delta) {
        Iterator<ProjetilInimigo> it = projeteisInimigos.iterator();
        while (it.hasNext()) {
            ProjetilInimigo p = it.next();
            p.avancar(delta);
            if (p.posicao().distancia(nave.posicao()) <= Nave.RAIO + ProjetilInimigo.RAIO) {
                it.remove();
                if (!nave.invulneravel()) {
                    nave.sofrerDano(ProjetilInimigo.DANO);
                    registrar(Evento.NAVE_ATINGIDA);
                }
            } else if (p.expirado() || !arena.contem(p.posicao())) {
                it.remove();
            }
        }
    }

    /** Ativa o poder especial (se estiver pronto e a partida rodando). */
    public void ativarPoder() {
        if (estado == EstadoPartida.EM_ANDAMENTO && poder.pronto()) {
            poder.ativar(this);
        }
    }

    public PoderEspecial poder() {
        return poder;
    }

    void dispararInimigo(ProjetilInimigo p) {
        projeteisInimigos.add(p);
    }

    public List<ProjetilInimigo> projeteisInimigos() {
        return Collections.unmodifiableList(projeteisInimigos);
    }

    /** Separação simples: inimigos sobrepostos se empurram metade da sobreposição cada. */
    private void separarInimigos() {
        for (int a = 0; a < inimigos.size(); a++) {
            for (int b = a + 1; b < inimigos.size(); b++) {
                Inimigo i = inimigos.get(a);
                Inimigo j = inimigos.get(b);
                Vetor d = i.posicao().menos(j.posicao());
                float minimo = i.tipo().raio() + j.tipo().raio();
                float dist = d.comprimento();
                if (dist < minimo && dist > 1e-4f) {
                    Vetor ajuste = d.normalizado().vezes((minimo - dist) / 2f);
                    i.empurrar(ajuste);
                    j.empurrar(ajuste.vezes(-1));
                }
            }
        }
    }

    private void recolherMortos() {
        Iterator<Inimigo> it = inimigos.iterator();
        while (it.hasNext()) {
            Inimigo i = it.next();
            if (i.morto()) {
                it.remove();
                inimigosDerrotados++;
                cristais.add(new CristalXp(i.posicao(), i.tipo().xp()));
                registrar(Evento.INIMIGO_DERROTADO);
            }
        }
    }

    private void coletarCristais(float delta) {
        Iterator<CristalXp> it = cristais.iterator();
        while (it.hasNext()) {
            CristalXp c = it.next();
            float dist = c.posicao().distancia(nave.posicao());
            if (dist <= CristalXp.RAIO_COLETA) {
                it.remove();
                ganharXp(c.valor());
                registrar(Evento.XP_COLETADO);
            } else if (dist <= nave.atributos().alcanceIma()) {
                c.atrairPara(nave.posicao(), delta);
            }
        }
    }

    void ganharXp(int valorBase) {
        xp += valorBase * nave.atributos().multiplicadorXp();
        while (xp >= xpParaProximoNivel()) {
            xp -= xpParaProximoNivel();
            nivel++;
            niveisPendentes++;
            registrar(Evento.SUBIU_NIVEL);
        }
    }

    private void resgatarPassageiros() {
        Iterator<Passageiro> it = passageirosNoMapa.iterator();
        while (it.hasNext()) {
            Passageiro p = it.next();
            if (nave.temEspaco() && p.posicao().distancia(nave.posicao()) <= Nave.RAIO + Passageiro.RAIO) {
                it.remove();
                nave.embarcar(p);
                registrar(Evento.PASSAGEIRO_RESGATADO);
            }
        }
    }

    private void verificarPlataforma() {
        if (!plataformaAberta && tempo >= TEMPO_MINIMO && passageirosResgatados() >= cronograma.total()) {
            plataformaAberta = true;
            registrar(Evento.PLATAFORMA_ABERTA);
            // Final: o chefe guarda a plataforma, cercado por uma horda.
            Inimigo chefe = new Inimigo(TipoInimigo.CHEFE, new Vetor(0, 3f));
            chefe.definirComportamento(new ComportamentoChefe());
            inimigos.add(chefe);
            for (int i = 0; i < HORDA_FINAL; i++) {
                inimigos.add(new Inimigo(TipoInimigo.PERSEGUIDOR,
                        arena.limitar(Vetor.polar((float) (2 * Math.PI * i / HORDA_FINAL), 7f))));
            }
            registrar(Evento.CHEFE_APARECEU);
        }
    }

    private void verificarFim() {
        if (nave.destruida()) {
            estado = EstadoPartida.DERROTA;
            registrar(Evento.DERROTA);
        } else if (plataformaAberta && nave.posicao().distancia(Vetor.ZERO) <= RAIO_PLATAFORMA) {
            estado = EstadoPartida.VITORIA;
            registrar(Evento.VITORIA);
        }
    }

    private void abrirEscolhaDeUpgrade() {
        opcoesUpgrade = sorteador.sortear(this, 3);
        if (opcoesUpgrade.isEmpty()) {
            niveisPendentes = 0;
            return;
        }
        estado = EstadoPartida.ESCOLHENDO_UPGRADE;
    }

    // ---- CampoDeBatalha (o que as armas podem usar) ----

    @Override
    public Vetor posicaoNave() {
        return nave.posicao();
    }

    @Override
    public Vetor direcaoNave() {
        return nave.direcao();
    }

    @Override
    public float multiplicadorDano() {
        return nave.atributos().multiplicadorDano();
    }

    @Override
    public float multiplicadorCadencia() {
        return nave.atributos().multiplicadorCadencia();
    }

    @Override
    public Optional<Inimigo> inimigoMaisProximo(float alcance) {
        return inimigos.stream()
                .filter(i -> !i.morto() && i.posicao().distancia(nave.posicao()) <= alcance)
                .min(Comparator.comparingDouble(i -> i.posicao().distancia(nave.posicao())));
    }

    @Override
    public List<Inimigo> inimigos() {
        return Collections.unmodifiableList(inimigos);
    }

    @Override
    public void disparar(Projetil projetil) {
        projeteis.add(projetil);
    }

    @Override
    public void causarDano(Inimigo inimigo, float dano) {
        if (nave.atributos().chanceCritico() > 0 && random.nextFloat() < nave.atributos().chanceCritico()) {
            dano *= 2f;
            registrar(Evento.CRITICO);
        }
        inimigo.sofrerDano(dano);
        registrar(Evento.INIMIGO_ATINGIDO);
    }

    @Override
    public void causarDanoEmArea(Vetor centro, float raio, float dano) {
        for (Inimigo i : inimigos) {
            if (i.posicao().distancia(centro) <= raio + i.tipo().raio()) {
                causarDano(i, dano);
            }
        }
    }

    @Override
    public float multiplicadorArea() {
        return nave.atributos().multiplicadorArea();
    }

    @Override
    public void empurrar(Inimigo inimigo, Vetor deslocamento) {
        inimigo.empurrar(deslocamento);
    }

    public int nivelPassiva(UpgradeAtributo passiva) {
        return niveisPassivas.getOrDefault(passiva, 0);
    }

    public void subirPassiva(UpgradeAtributo passiva) {
        niveisPassivas.merge(passiva, 1, Integer::sum);
    }

    public void registrarEvolucao(Evolucao e) {
        evolucoes.add(e);
        registrar(Evento.EVOLUCAO);
    }

    public List<Evolucao> evolucoes() {
        return Collections.unmodifiableList(evolucoes);
    }

    @Override
    public void registrar(Evento evento) {
        (atualizando ? eventosDoQuadro : eventosPendentes).add(evento);
    }

    // ---- usados por upgrades ----

    public <T extends Arma> Optional<T> arma(Class<T> tipo) {
        return arsenal.stream().filter(tipo::isInstance).map(tipo::cast).findFirst();
    }

    public void adicionarArma(Arma arma) {
        arsenal.add(arma);
    }

    public void curarNave(float valor) {
        nave.curar(valor);
    }

    // ---- ganchos para testes (mesmo pacote) ----

    void adicionarInimigo(Inimigo inimigo) {
        inimigos.add(inimigo);
    }

    void adicionarPassageiro(Passageiro passageiro) {
        passageirosNoMapa.add(passageiro);
    }

    List<Inimigo> inimigosParaTeste() {
        return inimigos;
    }

    void adicionarCristal(CristalXp cristal) {
        cristais.add(cristal);
    }

    // ---- leitura do estado (a tela desenha a partir daqui) ----

    public int xpParaProximoNivel() {
        return 4 + (nivel - 1) * 5;
    }

    /** inimigos×10 + segundos×2 + passageiros×100 + 500 se venceu. */
    public int pontuacao() {
        return inimigosDerrotados * 10 + (int) tempo * 2 + passageirosResgatados() * 100
                + (estado == EstadoPartida.VITORIA ? 500 : 0);
    }

    public int passageirosResgatados() {
        return nave.passageirosABordo().size();
    }

    public int totalPassageiros() {
        return cronograma.total();
    }

    public Optional<Passageiro> passageiroMaisProximo() {
        return passageirosNoMapa.stream()
                .min(Comparator.comparingDouble(p -> p.posicao().distancia(nave.posicao())));
    }

    public float tempoRestanteParaPlataforma() {
        return Math.max(0, TEMPO_MINIMO - tempo);
    }

    public Dificuldade dificuldade() {
        return dificuldade;
    }

    public Arena arena() {
        return arena;
    }

    public Nave nave() {
        return nave;
    }

    public List<Arma> arsenal() {
        return Collections.unmodifiableList(arsenal);
    }

    public List<Projetil> projeteis() {
        return Collections.unmodifiableList(projeteis);
    }

    public List<CristalXp> cristais() {
        return Collections.unmodifiableList(cristais);
    }

    public List<Passageiro> passageirosNoMapa() {
        return Collections.unmodifiableList(passageirosNoMapa);
    }

    public List<OpcaoUpgrade> opcoesUpgrade() {
        return opcoesUpgrade;
    }

    public EstadoPartida estado() {
        return estado;
    }

    public float tempo() {
        return tempo;
    }

    public int nivel() {
        return nivel;
    }

    public float xp() {
        return xp;
    }

    public int inimigosDerrotados() {
        return inimigosDerrotados;
    }

    public boolean plataformaAberta() {
        return plataformaAberta;
    }
}
