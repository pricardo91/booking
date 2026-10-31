package br.com.alura.booking.reserva.infra;

import br.com.alura.booking.reserva.application.ReservaRepository;
import br.com.alura.booking.reserva.domain.Reserva;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ReservaInfraRepository implements ReservaRepository {

    private final ReservaJPARepository reservaJPARepository;

    @Override
    public Reserva salva(Reserva reserva) {
        log.debug("[start] - {} - salva", getClass().getSimpleName());
        var reservaSalva = reservaJPARepository.saveAndFlush(reserva);
        log.debug("[finish] - {} - salva", getClass().getSimpleName());
        return reservaSalva;
    }
}
