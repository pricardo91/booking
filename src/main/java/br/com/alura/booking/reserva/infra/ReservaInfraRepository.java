package br.com.alura.booking.reserva.infra;

import br.com.alura.booking.reserva.application.ReservaFiltro;
import br.com.alura.booking.reserva.application.ReservaRepository;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.StatusReserva;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    @Override
    public Long buscaIdDaSalaDaReserva(Long idReserva) {
        log.debug("[start] - {} - buscaIdDaSalaDaReserva", getClass().getSimpleName());
        Long idSala = reservaJPARepository
                .buscaIdDaSala(idReserva)
                .orElseThrow(() -> new NegocioException(CodigoErro.RESERVA_NAO_ENCONTRADA, String.valueOf(idReserva)));
        log.debug("[finish] - {} - buscaIdDaSalaDaReserva", getClass().getSimpleName());
        return idSala;
    }

    @Override
    public Page<Reserva> listaReservas(ReservaFiltro filtro, Pageable pageable) {
        log.debug("[start] - {} - listaReservaHistorico", getClass().getSimpleName());

        Specification<Reserva> specification = Specification.allOf(
                ReservaSpecs.comStatus(filtro.status()),
                ReservaSpecs.daSala(filtro.idSala()),
                ReservaSpecs.doUsuario(filtro.idUsuario()));
        Page<Reserva> reservas = reservaJPARepository.findAll(specification, pageable);

        log.debug("[finish] - {} - listaReservaHistorico", getClass().getSimpleName());
        return reservas;
    }

    @Override
    public boolean existeReservaAtivaSobreposta(Reserva reserva) {
        log.debug("[start] - {} - existeReservaAtivaSobreposta", getClass().getSimpleName());

        boolean existe = reservaJPARepository.existeSobreposta(
                StatusReserva.ATIVO, reserva.getSala().getId(), reserva.getId(), reserva.getInicio(), reserva.getFim());

        log.debug("[finish] - {} - existeReservaAtivaSobreposta", getClass().getSimpleName());

        return existe;
    }

    @Override
    public List<Reserva> listaPendentesSobrepostas(Reserva reserva) {
        log.debug("[start] - {} - listaPendentesSobrepostas", getClass().getSimpleName());

        List<Reserva> listaReservasPendentes = reservaJPARepository.listaSobrepostas(
                StatusReserva.PENDENTE,
                reserva.getSala().getId(),
                reserva.getId(),
                reserva.getInicio(),
                reserva.getFim());

        log.debug("[finish] - {} - listaPendentesSobrepostas", getClass().getSimpleName());
        return listaReservasPendentes;
    }
}
