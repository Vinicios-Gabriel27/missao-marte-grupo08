package br.edu.unifor.missaomarte.servidor.repositorio;

import br.edu.unifor.missaomarte.servidor.dominio.Jogador;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JogadorRepositorio extends JpaRepository<Jogador, Long> {
    Optional<Jogador> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}
