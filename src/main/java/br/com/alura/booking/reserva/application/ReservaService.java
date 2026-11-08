package br.com.alura.booking.reserva.application;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaHistoricoResponse;
import br.com.alura.booking.reserva.api.response.ReservaResponse;
import br.com.alura.booking.reserva.api.response.ReservaResumoResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaService {
    ReservaResponse cadastraReserva(ReservaRequest novaReserva);

    ReservaResponse buscaReservaPorId(Long id);

    Page<ReservaResumoResponse> listaReservas(ReservaFiltro filtro, Pageable pageable);

    ReservaResponse confirmaReserva(Long id);

    ReservaResponse cancelaReserva(Long id);

    List<ReservaHistoricoResponse> historicoReserva(Long idReserva);

    void processaReservaVencida(Long idReserva, LocalDateTime agora);
}
