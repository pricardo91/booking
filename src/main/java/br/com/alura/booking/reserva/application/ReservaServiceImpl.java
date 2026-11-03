package br.com.alura.booking.reserva.application;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaResponse;
import br.com.alura.booking.reserva.api.response.ReservaResumoResponse;
import br.com.alura.booking.reserva.domain.MotivoHistorico;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.ReservaHistorico;
import br.com.alura.booking.reserva.domain.StatusReserva;
import br.com.alura.booking.sala.application.SalaRepository;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import br.com.alura.booking.usuario.application.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaServiceImpl implements ReservaService {

    private final SalaRepository salaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaHistoricoRepository reservaHistoricoRepository;

    @Override
    @Transactional
    public ReservaResponse cadastraReserva(ReservaRequest novaReserva) {
        log.debug("[start] - {} - cadastraReserva", getClass().getSimpleName());

        var sala = salaRepository.buscaSalaPorId(novaReserva.getSalaId());
        var usuario = usuarioRepository.buscaUsuarioPorId(novaReserva.getUsuarioId());

        Reserva reserva =
                reservaRepository.salva(new Reserva(usuario, sala, novaReserva.getInicio(), novaReserva.getFim()));

        ReservaResponse response = new ReservaResponse(reserva);

        log.debug("[finish] - {} - cadastraReserva", getClass().getSimpleName());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse buscaReservaPorId(Long id) {
        log.debug("[start] - {} - buscaReservaPorId", getClass().getSimpleName());
        Reserva reserva = reservaRepository.buscaReservaPorId(id);
        ReservaResponse response = new ReservaResponse(reserva);
        log.debug("[finish] - {} - buscaReservaPorId", getClass().getSimpleName());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResumoResponse> listaReservas(ReservaFiltro filtro, Pageable pageable) {
        log.debug("[start] - {} - listaReservaHistorico", getClass().getSimpleName());
        Page<Reserva> reservas = reservaRepository.listaReservas(filtro, pageable);
        log.debug("[finish] - {} - listaReservaHistorico", getClass().getSimpleName());
        return reservas.map(ReservaResumoResponse::new);
    }

    @Override
    @Transactional
    public ReservaResponse confirmaReserva(Long id) {
        log.debug("[start] - {} - confirmaReserva", getClass().getSimpleName());

        // A sala é travada ANTES de ler a reserva ou qualquer outra: assim todas as confirmações da
        // mesma sala esperam na fila (sem deadlock) e a reserva é lida já depois da espera.
        Long idSala = reservaRepository.buscaIdDaSalaDaReserva(id);
        salaRepository.travaSala(idSala);
        Reserva reserva = reservaRepository.buscaReservaPorId(id);

        if (reservaRepository.existeReservaAtivaSobreposta(reserva)) {
            throw new NegocioException(CodigoErro.RESERVA_CONFLITO_HORARIO);
        }

        List<Reserva> pendentes = reservaRepository.listaPendentesSobrepostas(reserva);

        StatusReserva statusAnterior = reserva.getStatus();
        reserva.confirmar();
        try {
            reservaRepository.salva(reserva);
        } catch (DataIntegrityViolationException exception) {
            throw new NegocioException(CodigoErro.RESERVA_CONFLITO_HORARIO);
        }

        reservaHistoricoRepository.registraReservaHistorico(
                new ReservaHistorico(reserva, statusAnterior, reserva.getStatus(), MotivoHistorico.CONFIRMACAO));

        for (Reserva pendente : pendentes) {
            StatusReserva statusPendenteAnterior = pendente.getStatus();
            pendente.cancelar();
            reservaRepository.salva(pendente);
            reservaHistoricoRepository.registraReservaHistorico(new ReservaHistorico(
                    pendente,
                    statusPendenteAnterior,
                    pendente.getStatus(),
                    MotivoHistorico.CONFLITO_COM_RESERVA_CONFIRMADA));
        }

        log.debug("[finish] - {} - confirmaReserva", getClass().getSimpleName());
        return new ReservaResponse(reserva);
    }

    @Override
    @Transactional
    public ReservaResponse cancelaReserva(Long id) {
        log.debug("[start] - {} - cancelaReserva", getClass().getSimpleName());

        Long idSala = reservaRepository.buscaIdDaSalaDaReserva(id);
        salaRepository.travaSala(idSala);

        Reserva reserva = reservaRepository.buscaReservaPorId(id);
        StatusReserva statusAntigo = reserva.getStatus();

        reserva.cancelar();
        reservaRepository.salva(reserva);

        reservaHistoricoRepository.registraReservaHistorico(new ReservaHistorico(
                reserva, statusAntigo, reserva.getStatus(), MotivoHistorico.CANCELAMENTO_PELO_USUARIO));

        log.debug("[finish] - {} - cancelaReserva", getClass().getSimpleName());
        return new ReservaResponse(reserva);
    }
}
