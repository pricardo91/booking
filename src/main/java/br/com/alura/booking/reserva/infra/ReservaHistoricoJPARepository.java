package br.com.alura.booking.reserva.infra;

import br.com.alura.booking.reserva.domain.ReservaHistorico;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaHistoricoJPARepository extends JpaRepository<ReservaHistorico, Long> {

    List<ReservaHistorico> findAllByReservaIdOrderByCriadoEmAscIdAsc(Long id);
}
