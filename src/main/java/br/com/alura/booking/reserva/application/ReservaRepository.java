package br.com.alura.booking.reserva.application;

import br.com.alura.booking.reserva.domain.Reserva;

public interface ReservaRepository {
    Reserva salva(Reserva reserva);
}
