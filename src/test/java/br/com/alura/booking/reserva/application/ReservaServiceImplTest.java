package br.com.alura.booking.reserva.application;

import static br.com.alura.booking.shared.exception.ErroAssert.assertErro;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaResponse;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.StatusReserva;
import br.com.alura.booking.sala.application.SalaRepository;
import br.com.alura.booking.sala.domain.Sala;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import br.com.alura.booking.usuario.application.UsuarioRepository;
import br.com.alura.booking.usuario.domain.Usuario;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
