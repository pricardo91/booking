package br.com.alura.booking.reserva.infra;

import br.com.alura.booking.reserva.application.ReservaHistoricoRepository;
import br.com.alura.booking.reserva.domain.ReservaHistorico;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ReservaHistoricoRepositoryImpl implements ReservaHistoricoRepository {

    private final ReservaHistoricoJPARepository reservaHistoricoJPARepository;

    @Override
    public void registraReservaHistorico(ReservaHistorico historico) {
        log.debug("[start] - {} - registraReservaHistorico", getClass().getSimpleName());
        reservaHistoricoJPARepository.saveAndFlush(historico);
        log.debug("[finish] - {} - registraReservaHistorico", getClass().getSimpleName());
    }

    @Override
    public List<ReservaHistorico> listaReservaHistorico(Long idReserva) {
        log.debug("[start] - {} - listaReservaHistorico", getClass().getSimpleName());

        List<ReservaHistorico> historicoLista =
                reservaHistoricoJPARepository.findAllByReservaIdOrderByCriadoEmAscIdAsc(idReserva);

        log.debug("[finish] - {} - listaReservaHistorico", getClass().getSimpleName());
        return historicoLista;
    }
}
