package br.com.alura.booking.reserva.application;

import br.com.alura.booking.reserva.domain.ReservaHistorico;
import java.util.List;

public interface ReservaHistoricoRepository {

    void registraReservaHistorico(ReservaHistorico historico);

    List<ReservaHistorico> listaReservaHistorico(Long idReserva);
}
