package br.com.alura.booking.reserva.application;

import br.com.alura.booking.reserva.domain.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaRepository {
    Reserva salva(Reserva reserva);

    Reserva buscaReservaPorId(Long id);

    Page<Reserva> listaReservas(ReservaFiltro filtro, Pageable pageable);
}
