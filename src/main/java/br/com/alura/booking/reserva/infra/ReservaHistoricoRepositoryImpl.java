package br.com.alura.booking.reserva.infra;

import br.com.alura.booking.reserva.application.ReservaHistoricoRepository;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.ReservaHistorico;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ReservaHistoricoRepositoryImpl implements ReservaHistoricoRepository {

    private final ReservaHistoricoJPARepository reservaHistoricoJPARepository;
    private final ReservaJPARepository reservaJPARepository;

    @Override
    public void registraReservaHistorico(ReservaHistorico historico) {
        log.debug("[start] - {} - registraReservaHistorico", getClass().getSimpleName());
        reservaHistoricoJPARepository.saveAndFlush(historico);
        log.debug("[finish] - {} - registraReservaHistorico", getClass().getSimpleName());
    }

    @Override
    public List<ReservaHistorico> listaReservaHistorico(Long id) {
        log.debug("[start] - {} - listaReservaHistorico", getClass().getSimpleName());

        Reserva reserva = reservaJPARepository
                .findById(id)
                .orElseThrow(() -> new NegocioException(CodigoErro.RESERVA_NAO_ENCONTRADA));

        List<ReservaHistorico> historicoList = reservaHistoricoJPARepository.findAllByReserva(reserva.getId());
        log.debug("[finish] - {} - listaReservaHistorico", getClass().getSimpleName());
        return historicoList;
    }
}
