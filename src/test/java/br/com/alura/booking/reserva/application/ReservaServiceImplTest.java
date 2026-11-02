package br.com.alura.booking.reserva.application;

import static br.com.alura.booking.shared.exception.ErroAssert.assertErro;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaResponse;
import br.com.alura.booking.reserva.api.response.ReservaResumoResponse;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.StatusReserva;
import br.com.alura.booking.sala.application.SalaRepository;
import br.com.alura.booking.sala.domain.Sala;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import br.com.alura.booking.usuario.application.UsuarioRepository;
import br.com.alura.booking.usuario.domain.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 12, 15, 10, 0);
    private static final LocalDateTime FIM = LocalDateTime.of(2026, 12, 15, 11, 0);

    @Mock
    private SalaRepository salaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private static Usuario usuario() {
        return new Usuario("Maria Souza", "maria@exemplo.com", "52998224725");
    }

    private static Sala sala() {
        return new Sala("Sala Azul", 10);
    }

    private static ReservaRequest pedido() {
        return new ReservaRequest(1L, 2L, INICIO, FIM);
    }

    @Test
    void cadastraReservaPendenteComUsuarioSalaEIntervaloInformados() {
        Usuario usuario = usuario();
        Sala sala = sala();
        when(usuarioRepository.buscaUsuarioPorId(1L)).thenReturn(usuario);
        when(salaRepository.buscaSalaPorId(2L)).thenReturn(sala);
        when(reservaRepository.salva(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));

        ReservaResponse resposta = reservaService.cadastraReserva(pedido());

        var salva = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository).salva(salva.capture());
        assertThat(salva.getValue().getUsuario()).isSameAs(usuario);
        assertThat(salva.getValue().getSala()).isSameAs(sala);
        assertThat(salva.getValue().getInicio()).isEqualTo(INICIO);
        assertThat(salva.getValue().getFim()).isEqualTo(FIM);
        assertThat(salva.getValue().getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(resposta.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(resposta.getInicio()).isEqualTo(INICIO);
        assertThat(resposta.getFim()).isEqualTo(FIM);
        assertThat(resposta.getUsuarioNome()).isEqualTo("Maria Souza");
        assertThat(resposta.getSalaNome()).isEqualTo("Sala Azul");
    }

    @Test
    void cadastraReservaRepassaUsuarioNaoEncontrado() {
        when(usuarioRepository.buscaUsuarioPorId(1L))
                .thenThrow(new NegocioException(CodigoErro.USUARIO_NAO_ENCONTRADO, "1"));

        assertErro(() -> reservaService.cadastraReserva(pedido()), CodigoErro.USUARIO_NAO_ENCONTRADO, "1");
        verify(reservaRepository, never()).salva(any());
    }

    @Test
    void cadastraReservaRepassaSalaNaoEncontrada() {
        when(salaRepository.buscaSalaPorId(2L)).thenThrow(new NegocioException(CodigoErro.SALA_NAO_ENCONTRADA, "2"));

        assertErro(() -> reservaService.cadastraReserva(pedido()), CodigoErro.SALA_NAO_ENCONTRADA, "2");
        verify(reservaRepository, never()).salva(any());
    }

    @Test
    void cadastraReservaRecusaUsuarioInativo() {
        Usuario usuario = usuario();
        usuario.inativar();
        when(usuarioRepository.buscaUsuarioPorId(1L)).thenReturn(usuario);
        when(salaRepository.buscaSalaPorId(2L)).thenReturn(sala());

        assertErro(() -> reservaService.cadastraReserva(pedido()), CodigoErro.USUARIO_INATIVO);
        verify(reservaRepository, never()).salva(any());
    }

    @Test
    void cadastraReservaRecusaSalaInativa() {
        Sala sala = sala();
        sala.inativar();
        when(usuarioRepository.buscaUsuarioPorId(1L)).thenReturn(usuario());
        when(salaRepository.buscaSalaPorId(2L)).thenReturn(sala);

        assertErro(() -> reservaService.cadastraReserva(pedido()), CodigoErro.SALA_INATIVA);
        verify(reservaRepository, never()).salva(any());
    }

    @Test
    void cadastraReservaRecusaIntervaloInvalido() {
        when(usuarioRepository.buscaUsuarioPorId(1L)).thenReturn(usuario());
        when(salaRepository.buscaSalaPorId(2L)).thenReturn(sala());
        var fimIgualAoInicio = new ReservaRequest(1L, 2L, INICIO, INICIO);

        assertErro(() -> reservaService.cadastraReserva(fimIgualAoInicio), CodigoErro.RESERVA_INTERVALO_INVALIDO);
        verify(reservaRepository, never()).salva(any());
    }

    @Test
    void buscaReservaPorIdDevolveReservaComUsuarioESala() {
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(new Reserva(usuario(), sala(), INICIO, FIM));

        ReservaResponse resposta = reservaService.buscaReservaPorId(5L);

        assertThat(resposta.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(resposta.getInicio()).isEqualTo(INICIO);
        assertThat(resposta.getFim()).isEqualTo(FIM);
        assertThat(resposta.getUsuarioNome()).isEqualTo("Maria Souza");
        assertThat(resposta.getSalaNome()).isEqualTo("Sala Azul");
    }

    @Test
    void buscaReservaPorIdRepassaReservaNaoEncontrada() {
        when(reservaRepository.buscaReservaPorId(99L))
                .thenThrow(new NegocioException(CodigoErro.RESERVA_NAO_ENCONTRADA, "99"));

        assertErro(() -> reservaService.buscaReservaPorId(99L), CodigoErro.RESERVA_NAO_ENCONTRADA, "99");
    }

    @Test
    void listaReservasMontaOResumoDeCadaReserva() {
        var filtro = new ReservaFiltro(StatusReserva.PENDENTE, 2L, 1L);
        Pageable pageable = PageRequest.of(0, 15);
        var reserva = new Reserva(usuario(), sala(), INICIO, FIM);
        when(reservaRepository.listaReservas(filtro, pageable))
                .thenReturn(new PageImpl<>(List.of(reserva), pageable, 1));

        Page<ReservaResumoResponse> pagina = reservaService.listaReservas(filtro, pageable);

        assertThat(pagina.getContent()).hasSize(1);
        ReservaResumoResponse resumo = pagina.getContent().get(0);
        assertThat(resumo.getUsuarioNome()).isEqualTo("Maria Souza");
        assertThat(resumo.getSalaNome()).isEqualTo("Sala Azul");
        assertThat(resumo.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(resumo.getInicio()).isEqualTo(INICIO);
        assertThat(resumo.getFim()).isEqualTo(FIM);
    }

    @Test
    void listaReservasMantemOTotalEAsPaginasDoRepositorio() {
        var filtro = new ReservaFiltro(null, null, null);
        Pageable pageable = PageRequest.of(1, 15);
        var reserva = new Reserva(usuario(), sala(), INICIO, FIM);
        when(reservaRepository.listaReservas(filtro, pageable))
                .thenReturn(new PageImpl<>(List.of(reserva), pageable, 31));

        Page<ReservaResumoResponse> pagina = reservaService.listaReservas(filtro, pageable);

        assertThat(pagina.getTotalElements()).isEqualTo(31);
        assertThat(pagina.getTotalPages()).isEqualTo(3);
        assertThat(pagina.getNumber()).isEqualTo(1);
    }

    @Test
    void listaReservasDevolvePaginaVaziaQuandoNaoHaReservas() {
        var filtro = new ReservaFiltro(StatusReserva.FINALIZADO, null, null);
        Pageable pageable = PageRequest.of(0, 15);
        when(reservaRepository.listaReservas(filtro, pageable)).thenReturn(Page.empty(pageable));

        Page<ReservaResumoResponse> pagina = reservaService.listaReservas(filtro, pageable);

        assertThat(pagina.getContent()).isEmpty();
        assertThat(pagina.getTotalElements()).isZero();
    }
}
