package br.com.alura.booking.sala.infra;

import br.com.alura.booking.sala.domain.Sala;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface SalaJPARepository extends JpaRepository<Sala, Long> {
    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, long id);

    /**
     * Busca a sala travando a linha dela (SELECT ... FOR UPDATE) até o fim da transação. Quem tentar
     * travar a mesma sala espera, por até 3 segundos, a transação que já a travou terminar.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select s from Sala s where s.id = :id")
    Optional<Sala> buscaSalaComTrava(@Param("id") long id);
}
