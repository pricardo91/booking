package br.com.alura.booking.reserva.application;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaResponse;
import br.com.alura.booking.reserva.api.response.ReservaResumoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaService {
    ReservaResponse cadastraReserva(ReservaRequest novaReserva);

    ReservaResponse buscaReservaPorId(Long id);

    Page<ReservaResumoResponse> listaReserva(ReservaFiltro filtro, Pageable pageable);
}
