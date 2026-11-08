package br.com.alura.booking.reserva.domain;

import java.time.LocalDateTime;

/**
 * Aviso de que uma reserva foi finalizada. Leva só valores simples (ids e o instante) de propósito:
 * quem escuta pode rodar depois que a transação fechou, quando uma {@link Reserva} com associações
 * LAZY já não consegue buscar sala e usuário no banco.
 *
 * @param idReserva a reserva que terminou
 * @param idSala a sala que ficou livre
 * @param idUsuario o dono da reserva
 * @param finalizadaEm o instante em que a reserva foi finalizada
 */
public record ReservaFinalizada(Long idReserva, Long idSala, Long idUsuario, LocalDateTime finalizadaEm) {}
