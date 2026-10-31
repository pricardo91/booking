package br.com.alura.booking.reserva.api;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaResponse;
import br.com.alura.booking.reserva.application.ReservaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReservaController implements ReservaAPI {

    private final ReservaService reservaService;

    @Override
    public ReservaResponse cadastraReserva(ReservaRequest novaReserva) {
        log.debug("[start] - {} - cadastraReserva", getClass().getSimpleName());
        ReservaResponse reservaNova = reservaService.cadastraReserva(novaReserva);
        log.debug("[finish] - {} - cadastraReserva", getClass().getSimpleName());
        return reservaNova;
    }

    @Override
    public ReservaResponse buscaReservaPorId(Long id) {
        log.debug("[start] - {} - buscaReservaPorId", getClass().getSimpleName());
        ReservaResponse response = reservaService.buscaReservaPorId(id);
        log.debug("[finish] - {} - buscaReservaPorId", getClass().getSimpleName());
        return response;
    }
}
