package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Arma;
import br.edu.unifor.missaomarte.sobrevivencia.armas.MisseisTeleguiados;
import br.edu.unifor.missaomarte.sobrevivencia.armas.OndaDeChoque;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Orbital;
import br.edu.unifor.missaomarte.sobrevivencia.armas.RaioEmCadeia;
import br.edu.unifor.missaomarte.sobrevivencia.armas.TiroAutomatico;

/**
 * "Cartas cruéis": arma no nível máximo + a passiva certa liberam a evolução da arma
 * (ideia das evoluções de Vampire Survivors). Cada evolução aparece no máximo uma vez.
 */
public enum Evolucao implements Upgrade {
    TEMPESTADE_DE_PLASMA(TiroAutomatico.class, UpgradeAtributo.CADENCIA, "Rajadas de plasma em 360° sem parar"),
    ANEL_DE_SATURNO(Orbital.class, UpgradeAtributo.AREA, "6 esferas gigantes girando rápido"),
    TERREMOTO_MARCIANO(OndaDeChoque.class, UpgradeAtributo.ARMADURA, "Ondas enormes que empurram a horda"),
    CHUVA_DE_METEOROS(MisseisTeleguiados.class, UpgradeAtributo.CRITICO, "Salvas de 5 mísseis com explosões enormes"),
    TEMPESTADE_ELETRICA(RaioEmCadeia.class, UpgradeAtributo.VELOCIDADE, "Raios rápidos saltando por 14 inimigos");

    private final Class<? extends Arma> arma;
    private final UpgradeAtributo passiva;
    private final String descricao;

    Evolucao(Class<? extends Arma> arma, UpgradeAtributo passiva, String descricao) {
        this.arma = arma;
        this.passiva = passiva;
        this.descricao = descricao;
    }

    public UpgradeAtributo passiva() {
        return passiva;
    }

    public Class<? extends Arma> arma() {
        return arma;
    }

    @Override
    public String titulo() {
        return switch (this) {
            case TEMPESTADE_DE_PLASMA -> "Tempestade de Plasma";
            case ANEL_DE_SATURNO -> "Anel de Saturno";
            case TERREMOTO_MARCIANO -> "Terremoto Marciano";
            case CHUVA_DE_METEOROS -> "Chuva de Meteoros";
            case TEMPESTADE_ELETRICA -> "Tempestade Elétrica";
        };
    }

    @Override
    public String descricao() {
        return descricao;
    }

    @Override
    public String chave() {
        return "evolucao_" + name().toLowerCase();
    }

    @Override
    public int nivelAtual(PartidaSobrevivencia partida) {
        return 0;
    }

    @Override
    public int nivelMaximo() {
        return 1;
    }

    /** Disponível quando a arma está no nível máximo, a passiva foi pega e ainda não evoluiu. */
    @Override
    public boolean disponivel(PartidaSobrevivencia partida) {
        return partida.arma(arma).map(a -> a.noNivelMaximo() && !a.evoluida()).orElse(false)
                && partida.nivelPassiva(passiva) >= 1;
    }

    @Override
    public void aplicar(PartidaSobrevivencia partida, Raridade raridade) {
        partida.arma(arma).ifPresent(a -> {
            a.evoluir();
            partida.registrarEvolucao(this);
        });
    }
}
