package br.com.alura.booking.reserva.infra;

import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.StatusReserva;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaJPARepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "sala"})
    Page<Reserva> findAll(Specification<Reserva> spec, Pageable pageable);

    /** Só o id da sala da reserva. Devolve um valor simples, sem carregar a reserva para o JPA. */
    @Query("select r.sala.id from Reserva r where r.id = :id")
    Optional<Long> buscaIdDaSala(@Param("id") Long id);

    @Query("""
        select count(r) > 0 from Reserva r
        where r.sala.id = :salaId
          and r.status = :status
          and r.id <> :id
          and r.inicio < :fim
          and r.fim > :inicio
        """)
    boolean existeSobreposta(
            @Param("status") StatusReserva statusReserva,
            @Param("salaId") Long salaId,
            @Param("id") Long id,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    @Query("""
        select r from Reserva r
        where r.sala.id = :salaId
          and r.status = :status
          and r.id <> :id
          and r.inicio < :fim
          and r.fim > :inicio
        """)
    List<Reserva> listaSobrepostas(
            @Param("status") StatusReserva statusReserva,
            @Param("salaId") Long salaId,
            @Param("id") Long id,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}
