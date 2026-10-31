package br.com.alura.booking.reserva.infra;

import br.com.alura.booking.reserva.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaJPARepository extends JpaRepository<Reserva, Long> {}
