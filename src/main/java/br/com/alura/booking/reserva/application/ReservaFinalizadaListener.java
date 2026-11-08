package br.com.alura.booking.reserva.application;

import br.com.alura.booking.reserva.domain.ReservaFinalizada;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class ReservaFinalizadaListener {

    @TransactionalEventListener
    public void registraReservaFinalizada(ReservaFinalizada reservaFinalizada) {
        log.info(
                "Reserva {} finalizada: sala {} liberada, usuário {}",
                reservaFinalizada.idReserva(),
                reservaFinalizada.idSala(),
                reservaFinalizada.idUsuario());
    }
}
