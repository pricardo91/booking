package br.com.alura.booking.sala.infra;

import br.com.alura.booking.sala.domain.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaJPARepository extends JpaRepository<Sala, Long> {
    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, long id);
}
