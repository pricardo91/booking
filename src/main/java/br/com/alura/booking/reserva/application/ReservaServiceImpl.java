package br.com.alura.booking.reserva.application;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaResponse;
import br.com.alura.booking.reserva.api.response.ReservaResumoResponse;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.sala.application.SalaRepository;
import br.com.alura.booking.usuario.application.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        log.debug("[start] - {} - listaReservas", getClass().getSimpleName());
        Page<Reserva> reservas = reservaRepository.listaReservas(filtro, pageable);
        log.debug("[finish] - {} - listaReservas", getClass().getSimpleName());
        return reservas.map(ReservaResumoResponse::new);
    }
}
