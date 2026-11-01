package br.com.alura.booking.reserva.infra;

import br.com.alura.booking.reserva.domain.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReservaJPARepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "sala"})
    Page<Reserva> findAll(Specification<Reserva> spec, Pageable pageable);
}
