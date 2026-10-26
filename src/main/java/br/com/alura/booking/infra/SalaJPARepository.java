package br.com.alura.booking.infra;

import br.com.alura.booking.domain.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaJPARepository extends JpaRepository<Sala, Long> {
    boolean existsByNomeIgnoreCase(String nome);
}
