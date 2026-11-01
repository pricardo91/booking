package br.com.alura.booking.reserva.application;

import br.com.alura.booking.reserva.domain.StatusReserva;

public record ReservaFiltro(StatusReserva status, Long idSala, Long idUsuario) {}
