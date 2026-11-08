package br.com.alura.booking.reserva.application;

import static br.com.alura.booking.shared.exception.ErroAssert.assertErro;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaHistoricoResponse;
import br.com.alura.booking.reserva.api.response.ReservaResponse;
import br.com.alura.booking.reserva.api.response.ReservaResumoResponse;
import br.com.alura.booking.reserva.domain.MotivoHistorico;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.ReservaFinalizada;
import br.com.alura.booking.reserva.domain.ReservaHistorico;
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
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 12, 15, 10, 0);
    private static final LocalDateTime FIM = LocalDateTime.of(2026, 12, 15, 11, 0);
    private static final LocalDateTime AGORA = FIM.plusSeconds(2);

    @Mock
    private SalaRepository salaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ReservaHistoricoRepository reservaHistoricoRepository;

    @Mock
    private ApplicationEventPublisher publicador;

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

    // ---------- confirmaReserva ----------

    private static Reserva pendente() {
        return new Reserva(usuario(), sala(), INICIO, FIM);
    }

    @Test
    void confirmaReservaPendenteEGravaOHistorico() {
        Reserva reserva = pendente();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        ReservaResponse resposta = reservaService.confirmaReserva(5L);

        assertThat(resposta.getStatus()).isEqualTo(StatusReserva.ATIVO);
        verify(reservaRepository).salva(reserva);
        var historico = ArgumentCaptor.forClass(ReservaHistorico.class);
        verify(reservaHistoricoRepository).registraReservaHistorico(historico.capture());
        assertThat(historico.getValue().getReserva()).isSameAs(reserva);
        assertThat(historico.getValue().getStatusAnterior()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(historico.getValue().getStatusNovo()).isEqualTo(StatusReserva.ATIVO);
        assertThat(historico.getValue().getMotivo()).isEqualTo(MotivoHistorico.CONFIRMACAO);
    }

    @Test
    void confirmaReservaCancelaAsPendentesQueDisputamOHorario() {
        Reserva reserva = pendente();
        Reserva concorrenteUm = pendente();
        Reserva concorrenteDois = pendente();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);
        when(reservaRepository.listaPendentesSobrepostas(reserva)).thenReturn(List.of(concorrenteUm, concorrenteDois));

        reservaService.confirmaReserva(5L);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.ATIVO);
        assertThat(concorrenteUm.getStatus()).isEqualTo(StatusReserva.CANCELADO);
        assertThat(concorrenteDois.getStatus()).isEqualTo(StatusReserva.CANCELADO);
        verify(reservaRepository).salva(concorrenteUm);
        verify(reservaRepository).salva(concorrenteDois);
        var historicos = ArgumentCaptor.forClass(ReservaHistorico.class);
        verify(reservaHistoricoRepository, times(3)).registraReservaHistorico(historicos.capture());
        assertThat(historicos.getAllValues())
                .extracting(ReservaHistorico::getReserva, ReservaHistorico::getMotivo)
                .containsExactly(
                        tuple(reserva, MotivoHistorico.CONFIRMACAO),
                        tuple(concorrenteUm, MotivoHistorico.CONFLITO_COM_RESERVA_CONFIRMADA),
                        tuple(concorrenteDois, MotivoHistorico.CONFLITO_COM_RESERVA_CONFIRMADA));
    }

    @Test
    void confirmaReservaRecusaQuandoJaExisteReservaAtivaNoHorario() {
        Reserva reserva = pendente();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);
        when(reservaRepository.existeReservaAtivaSobreposta(reserva)).thenReturn(true);

        assertErro(() -> reservaService.confirmaReserva(5L), CodigoErro.RESERVA_CONFLITO_HORARIO);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        verify(reservaRepository, never()).salva(any());
        verify(reservaHistoricoRepository, never()).registraReservaHistorico(any());
    }

    @Test
    void confirmaReservaTraduzAViolacaoDaConstraintEmConflitoDeHorario() {
        Reserva reserva = pendente();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);
        when(reservaRepository.salva(reserva))
                .thenThrow(new DataIntegrityViolationException("ex_reserva_sem_sobreposicao"));

        assertErro(() -> reservaService.confirmaReserva(5L), CodigoErro.RESERVA_CONFLITO_HORARIO);

        verify(reservaHistoricoRepository, never()).registraReservaHistorico(any());
    }

    @Test
    void confirmaReservaRecusaReservaQueNaoEstaPendente() {
        Reserva reserva = pendente();
        reserva.confirmar();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        assertErro(() -> reservaService.confirmaReserva(5L), CodigoErro.RESERVA_SO_PENDENTE_CONFIRMA);

        verify(reservaRepository, never()).salva(any());
        verify(reservaHistoricoRepository, never()).registraReservaHistorico(any());
    }

    @Test
    void confirmaReservaRepassaReservaNaoEncontrada() {
        when(reservaRepository.buscaIdDaSalaDaReserva(99L))
                .thenThrow(new NegocioException(CodigoErro.RESERVA_NAO_ENCONTRADA, "99"));

        assertErro(() -> reservaService.confirmaReserva(99L), CodigoErro.RESERVA_NAO_ENCONTRADA, "99");

        verify(salaRepository, never()).travaSala(anyLong());
        verify(reservaRepository, never()).salva(any());
    }

    @Test
    void confirmaReservaTravaASalaAntesDeLerAReserva() {
        Reserva reserva = pendente();
        when(reservaRepository.buscaIdDaSalaDaReserva(5L)).thenReturn(2L);
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        reservaService.confirmaReserva(5L);

        // A ordem é o que evita o deadlock: sala primeiro, reserva depois.
        InOrder ordem = inOrder(reservaRepository, salaRepository);
        ordem.verify(reservaRepository).buscaIdDaSalaDaReserva(5L);
        ordem.verify(salaRepository).travaSala(2L);
        ordem.verify(reservaRepository).buscaReservaPorId(5L);
    }

    @Test
    void confirmaReservaRepassaSalaNaoEncontradaAoTravar() {
        when(reservaRepository.buscaIdDaSalaDaReserva(5L)).thenReturn(2L);
        doThrow(new NegocioException(CodigoErro.SALA_NAO_ENCONTRADA, "2"))
                .when(salaRepository)
                .travaSala(2L);

        assertErro(() -> reservaService.confirmaReserva(5L), CodigoErro.SALA_NAO_ENCONTRADA, "2");

        verify(reservaRepository, never()).buscaReservaPorId(any());
        verify(reservaRepository, never()).salva(any());
    }

    // ---------- cancelaReserva ----------

    private static Reserva ativa() {
        Reserva reserva = pendente();
        reserva.confirmar();
        return reserva;
    }

    @Test
    void cancelaReservaPendenteEGravaOHistorico() {
        Reserva reserva = pendente();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        ReservaResponse resposta = reservaService.cancelaReserva(5L);

        assertThat(resposta.getStatus()).isEqualTo(StatusReserva.CANCELADO);
        verify(reservaRepository).salva(reserva);
        var historico = ArgumentCaptor.forClass(ReservaHistorico.class);
        verify(reservaHistoricoRepository).registraReservaHistorico(historico.capture());
        assertThat(historico.getValue().getReserva()).isSameAs(reserva);
        assertThat(historico.getValue().getStatusAnterior()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(historico.getValue().getStatusNovo()).isEqualTo(StatusReserva.CANCELADO);
        assertThat(historico.getValue().getMotivo()).isEqualTo(MotivoHistorico.CANCELAMENTO_PELO_USUARIO);
    }

    @Test
    void cancelaReservaAtivaGuardandoOStatusAnteriorNoHistorico() {
        Reserva reserva = ativa();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        ReservaResponse resposta = reservaService.cancelaReserva(5L);

        assertThat(resposta.getStatus()).isEqualTo(StatusReserva.CANCELADO);
        var historico = ArgumentCaptor.forClass(ReservaHistorico.class);
        verify(reservaHistoricoRepository).registraReservaHistorico(historico.capture());
        assertThat(historico.getValue().getStatusAnterior()).isEqualTo(StatusReserva.ATIVO);
        assertThat(historico.getValue().getStatusNovo()).isEqualTo(StatusReserva.CANCELADO);
    }

    @Test
    void cancelaReservaRecusaReservaJaCancelada() {
        Reserva reserva = pendente();
        reserva.cancelar();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        assertErro(() -> reservaService.cancelaReserva(5L), CodigoErro.RESERVA_JA_CANCELADA);

        verify(reservaRepository, never()).salva(any());
        verify(reservaHistoricoRepository, never()).registraReservaHistorico(any());
    }

    @Test
    void cancelaReservaRecusaReservaFinalizada() {
        Reserva reserva = ativa();
        reserva.finalizar(FIM);
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        assertErro(() -> reservaService.cancelaReserva(5L), CodigoErro.RESERVA_FINALIZADA_NAO_CANCELA);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.FINALIZADO);
        verify(reservaRepository, never()).salva(any());
        verify(reservaHistoricoRepository, never()).registraReservaHistorico(any());
    }

    @Test
    void cancelaReservaRepassaReservaNaoEncontrada() {
        when(reservaRepository.buscaIdDaSalaDaReserva(99L))
                .thenThrow(new NegocioException(CodigoErro.RESERVA_NAO_ENCONTRADA, "99"));

        assertErro(() -> reservaService.cancelaReserva(99L), CodigoErro.RESERVA_NAO_ENCONTRADA, "99");

        verify(salaRepository, never()).travaSala(anyLong());
        verify(reservaRepository, never()).salva(any());
    }

    @Test
    void cancelaReservaTravaASalaAntesDeLerAReserva() {
        Reserva reserva = pendente();
        when(reservaRepository.buscaIdDaSalaDaReserva(5L)).thenReturn(2L);
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        reservaService.cancelaReserva(5L);

        // Mesma ordem do confirma: todas as operações da sala entram na mesma fila.
        InOrder ordem = inOrder(reservaRepository, salaRepository);
        ordem.verify(reservaRepository).buscaIdDaSalaDaReserva(5L);
        ordem.verify(salaRepository).travaSala(2L);
        ordem.verify(reservaRepository).buscaReservaPorId(5L);
    }

    @Test
    void cancelaReservaRepassaSalaNaoEncontradaAoTravar() {
        when(reservaRepository.buscaIdDaSalaDaReserva(5L)).thenReturn(2L);
        doThrow(new NegocioException(CodigoErro.SALA_NAO_ENCONTRADA, "2"))
                .when(salaRepository)
                .travaSala(2L);

        assertErro(() -> reservaService.cancelaReserva(5L), CodigoErro.SALA_NAO_ENCONTRADA, "2");

        verify(reservaRepository, never()).buscaReservaPorId(any());
        verify(reservaRepository, never()).salva(any());
    }

    // ---------- processaReservaVencida ----------

    /** Reserva ATIVO com ids definidos (usuário 1, sala 2, reserva 5), para conferir o evento. */
    private static Reserva ativaComIds() {
        Reserva reserva = ativa();
        ReflectionTestUtils.setField(reserva, "id", 5L);
        ReflectionTestUtils.setField(reserva.getSala(), "id", 2L);
        ReflectionTestUtils.setField(reserva.getUsuario(), "id", 1L);
        return reserva;
    }

    @Test
    void processaReservaVencidaFinalizaAAtivaGravaOHistoricoEPublicaOEvento() {
        Reserva reserva = ativaComIds();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        reservaService.processaReservaVencida(5L, AGORA);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.FINALIZADO);
        verify(reservaRepository).salva(reserva);
        var historico = ArgumentCaptor.forClass(ReservaHistorico.class);
        verify(reservaHistoricoRepository).registraReservaHistorico(historico.capture());
        assertThat(historico.getValue().getStatusAnterior()).isEqualTo(StatusReserva.ATIVO);
        assertThat(historico.getValue().getStatusNovo()).isEqualTo(StatusReserva.FINALIZADO);
        assertThat(historico.getValue().getMotivo()).isEqualTo(MotivoHistorico.FINALIZACAO_AUTOMATICA);
        verify(publicador).publishEvent(new ReservaFinalizada(5L, 2L, 1L, AGORA));
    }

    @Test
    void processaReservaVencidaCancelaAPendenteSemConfirmacaoSemPublicarEvento() {
        Reserva reserva = pendente();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        reservaService.processaReservaVencida(5L, AGORA);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADO);
        verify(reservaRepository).salva(reserva);
        var historico = ArgumentCaptor.forClass(ReservaHistorico.class);
        verify(reservaHistoricoRepository).registraReservaHistorico(historico.capture());
        assertThat(historico.getValue().getStatusAnterior()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(historico.getValue().getStatusNovo()).isEqualTo(StatusReserva.CANCELADO);
        assertThat(historico.getValue().getMotivo()).isEqualTo(MotivoHistorico.EXPIRACAO_SEM_CONFIRMACAO);
        verifyNoInteractions(publicador);
    }

    @Test
    void processaReservaVencidaNaoFazNadaComReservaJaFinalizada() {
        Reserva reserva = ativa();
        reserva.finalizar(FIM);
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        reservaService.processaReservaVencida(5L, AGORA);

        // Idempotente: rodar de novo sobre uma reserva já tratada não muda nada.
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.FINALIZADO);
        verify(reservaRepository, never()).salva(any());
        verify(reservaHistoricoRepository, never()).registraReservaHistorico(any());
        verifyNoInteractions(publicador);
    }

    @Test
    void processaReservaVencidaNaoFazNadaComReservaJaCancelada() {
        Reserva reserva = pendente();
        reserva.cancelar();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        reservaService.processaReservaVencida(5L, AGORA);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADO);
        verify(reservaRepository, never()).salva(any());
        verify(reservaHistoricoRepository, never()).registraReservaHistorico(any());
        verifyNoInteractions(publicador);
    }

    @Test
    void processaReservaVencidaRecusaAtivaCujoHorarioAindaNaoTerminou() {
        Reserva reserva = ativa();
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        assertErro(
                () -> reservaService.processaReservaVencida(5L, FIM.minusMinutes(1)),
                CodigoErro.RESERVA_AINDA_NAO_TERMINOU);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.ATIVO);
        verify(reservaRepository, never()).salva(any());
        verify(reservaHistoricoRepository, never()).registraReservaHistorico(any());
        verifyNoInteractions(publicador);
    }

    @Test
    void processaReservaVencidaTravaASalaAntesDeLerAReserva() {
        Reserva reserva = pendente();
        when(reservaRepository.buscaIdDaSalaDaReserva(5L)).thenReturn(2L);
        when(reservaRepository.buscaReservaPorId(5L)).thenReturn(reserva);

        reservaService.processaReservaVencida(5L, AGORA);

        // Mesma fila de confirmar e cancelar: toda mudança de status da sala passa pelo lock da sala.
        InOrder ordem = inOrder(reservaRepository, salaRepository);
        ordem.verify(reservaRepository).buscaIdDaSalaDaReserva(5L);
        ordem.verify(salaRepository).travaSala(2L);
        ordem.verify(reservaRepository).buscaReservaPorId(5L);
    }

    // ---------- historicoReserva ----------

    @Test
    void historicoReservaConverteCadaRegistroMantendoAOrdemRecebida() {
        Reserva reserva = pendente();
        var confirmacao =
                new ReservaHistorico(reserva, StatusReserva.PENDENTE, StatusReserva.ATIVO, MotivoHistorico.CONFIRMACAO);
        var cancelamento = new ReservaHistorico(
                reserva, StatusReserva.ATIVO, StatusReserva.CANCELADO, MotivoHistorico.CANCELAMENTO_PELO_USUARIO);
        when(reservaHistoricoRepository.listaReservaHistorico(5L)).thenReturn(List.of(confirmacao, cancelamento));

        List<ReservaHistoricoResponse> resposta = reservaService.historicoReserva(5L);

        assertThat(resposta)
                .extracting(
                        ReservaHistoricoResponse::getStatusAnterior,
                        ReservaHistoricoResponse::getStatusNovo,
                        ReservaHistoricoResponse::getMotivo)
                .containsExactly(
                        tuple(StatusReserva.PENDENTE, StatusReserva.ATIVO, MotivoHistorico.CONFIRMACAO),
                        tuple(StatusReserva.ATIVO, StatusReserva.CANCELADO, MotivoHistorico.CANCELAMENTO_PELO_USUARIO));
    }

    @Test
    void historicoReservaDevolveListaVaziaQuandoAReservaNaoMudouDeStatus() {
        when(reservaHistoricoRepository.listaReservaHistorico(5L)).thenReturn(List.of());

        assertThat(reservaService.historicoReserva(5L)).isEmpty();
    }

    @Test
    void historicoReservaConfereAExistenciaDaReservaAntesDeListar() {
        when(reservaHistoricoRepository.listaReservaHistorico(5L)).thenReturn(List.of());

        reservaService.historicoReserva(5L);

        InOrder ordem = inOrder(reservaRepository, reservaHistoricoRepository);
        ordem.verify(reservaRepository).buscaReservaPorId(5L);
        ordem.verify(reservaHistoricoRepository).listaReservaHistorico(5L);
    }

    @Test
    void historicoReservaRepassaReservaNaoEncontradaSemConsultarOHistorico() {
        when(reservaRepository.buscaReservaPorId(99L))
                .thenThrow(new NegocioException(CodigoErro.RESERVA_NAO_ENCONTRADA, "99"));

        assertErro(() -> reservaService.historicoReserva(99L), CodigoErro.RESERVA_NAO_ENCONTRADA, "99");

        verifyNoInteractions(reservaHistoricoRepository);
    }
}
