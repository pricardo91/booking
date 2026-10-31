package br.com.alura.booking.reserva.infra;

import br.com.alura.booking.reserva.application.ReservaRepository;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
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

    @Override
    public Reserva buscaReservaPorId(Long id) {
        log.debug("[start] - {} - buscaReservaPorId", getClass().getSimpleName());
        Reserva reserva = reservaJPARepository
                .findById(id)
                .orElseThrow(() -> new NegocioException(CodigoErro.RESERVA_NAO_ENCONTRADA, String.valueOf(id)));
        log.debug("[finish] - {} - buscaReservaPorId", getClass().getSimpleName());
        return reserva;
    }
}
