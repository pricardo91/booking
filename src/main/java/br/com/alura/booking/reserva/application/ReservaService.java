package br.com.alura.booking.reserva.application;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaResponse;

public interface ReservaService {
    ReservaResponse cadastraReserva(ReservaRequest novaReserva);

    ReservaResponse buscaReservaPorId(Long id);
}
