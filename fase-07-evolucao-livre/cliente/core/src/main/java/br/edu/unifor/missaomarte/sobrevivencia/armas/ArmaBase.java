package br.edu.unifor.missaomarte.sobrevivencia.armas;

/** Nível e evolução comuns a todas as armas (evita repetir em cada uma). */
abstract class ArmaBase implements Arma {
    protected int nivel = 1;
    protected boolean evoluida;

    @Override
    public int nivel() {
        return nivel;
    }

    @Override
    public void subirNivel() {
        if (!noNivelMaximo()) {
            nivel++;
        }
    }

    @Override
    public boolean evoluida() {
        return evoluida;
    }

    @Override
    public void evoluir() {
        evoluida = true;
    }
}
