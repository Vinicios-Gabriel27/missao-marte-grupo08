package br.edu.unifor.missaomarte.servidor.repositorio;

import br.edu.unifor.missaomarte.servidor.dominio.Jogador;
import br.edu.unifor.missaomarte.servidor.dominio.NivelDificuldade;
import br.edu.unifor.missaomarte.servidor.dominio.PartidaRegistrada;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidaRepositorio extends JpaRepository<PartidaRegistrada, Long> {
    @Query("select p from PartidaRegistrada p join fetch p.jogador "
            + "where (:dificuldade is null or p.dificuldade = :dificuldade) "
            + "order by p.pontuacao desc, p.tempoSegundos asc, p.id asc")
    List<PartidaRegistrada> melhores(@Param("dificuldade") NivelDificuldade dificuldade, Pageable pagina);

    @EntityGraph(attributePaths = "jogador")
    List<PartidaRegistrada> findByJogadorOrderByRegistradaEmDesc(Jogador jogador, Pageable pagina);

    long countByJogador(Jogador jogador);

    long countByJogadorAndVenceuTrue(Jogador jogador);

    @Query("select coalesce(max(p.pontuacao), 0) from PartidaRegistrada p where p.jogador = :jogador")
    int melhorPontuacao(@Param("jogador") Jogador jogador);

    @Query("select count(p) + 1 from PartidaRegistrada p where p.pontuacao > :pontuacao")
    long posicaoDaPontuacao(@Param("pontuacao") int pontuacao);
}
