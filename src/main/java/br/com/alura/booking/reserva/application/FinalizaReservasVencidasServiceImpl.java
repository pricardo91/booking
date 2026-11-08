package br.com.alura.booking.reserva.application;

import br.com.alura.booking.reserva.domain.StatusReserva;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinalizaReservasVencidasServiceImpl implements FinalizaReservasVencidasService {

    private final ReservaRepository reservaRepository;
    private final ReservaService reservaService;
    private final Clock clock;

    @Value("${booking.job.finalizacao.tamanho-lote}")
    private int tamanhoLote;

    @Override
    public void processaReservasVencidas() {
        log.debug("[start] - {} - processaReservasVencidas", getClass().getSimpleName());

        var agora = LocalDateTime.now(clock);
        List<Long> idReservasAtivasVencidas =
                reservaRepository.buscaIdReservaVencidaPorStatus(agora, StatusReserva.ATIVO, tamanhoLote);

        List<Long> idReservasPendentesVencidas =
                reservaRepository.buscaIdReservaVencidaPorStatus(agora, StatusReserva.PENDENTE, tamanhoLote);

        List<Long> listaCombinada = Stream.of(idReservasAtivasVencidas, idReservasPendentesVencidas)
                .flatMap(List::stream)
                .toList();

        int processadas = 0;
        int falhas = 0;

        for (Long id : listaCombinada) {
            try {
                reservaService.processaReservaVencida(id, agora);
                processadas++;
            } catch (Exception e) {
                log.error("Erro no id {}.", id, e);
                falhas++;
            }
        }
        log.info("Processadas com sucesso: {} | Processadas com erro: {}", processadas, falhas);

        log.debug("[finish] - {} - processaReservasVencidas", getClass().getSimpleName());
    }
}
