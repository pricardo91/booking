package br.com.alura.booking.domain;

/**
 * Ciclo de vida da reserva. Em vez de apagar uma reserva cancelada, mantemos o registro com status
 * CANCELADO: preserva o histórico para relatórios e libera o horário.
 *
 * <p>Transições previstas:
 *
 * <ul>
 *   <li>PENDENTE → ATIVO (confirmação)
 *   <li>PENDENTE → CANCELADO
 *   <li>ATIVO → CANCELADO
 *   <li>ATIVO → FINALIZADO (o horário da reserva terminou)
 *   <li>CANCELADO e FINALIZADO são estados finais: não voltam a nenhum outro. Para reservar de
 *       novo, cria-se uma nova reserva, que passa pela checagem de conflito.
 * </ul>
 *
 * <p>Só ATIVO ocupa a sala. PENDENTE não bloqueia o horário: várias reservas pendentes podem
 * disputar a mesma sala, e vale o primeiro a ser confirmado. Ao confirmar, o serviço cancela as
 * outras pendentes que se sobrepõem. As transições são aplicadas por confirmar(), cancelar() e
 * finalizar() em {@link Reserva}. Quem chama finalizar() quando o horário termina é o serviço (por
 * exemplo, um job agendado).
 */
public enum StatusReserva {
    PENDENTE, // criada, ainda não confirmada
    ATIVO, // confirmada; ocupa a sala no intervalo
    CANCELADO, // não ocupa mais a sala
    FINALIZADO // o horário terminou; não ocupa mais a sala e não pode mais ser cancelada
}
