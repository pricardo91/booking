package br.com.alura.booking.reserva.job;

import br.com.alura.booking.reserva.application.FinalizaReservasVencidasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinalizaReservasVencidasJob {

    private final FinalizaReservasVencidasService finalizaReservasVencidasService;

    @SchedulerLock(name = "finalizaReservasVencidas", lockAtMostFor = "PT5M", lockAtLeastFor = "PT2S")
    @Scheduled(fixedDelayString = "${booking.job.finalizacao.intervalo}")
    public void finalizaReservas() {
        log.debug("[start] - {} - finalizaReservas", getClass().getSimpleName());
        finalizaReservasVencidasService.processaReservasVencidas();
        log.debug("[finish] - {} - finalizaReservas", getClass().getSimpleName());
    }
}
