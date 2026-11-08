package br.com.alura.booking.reserva.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.alura.booking.reserva.domain.StatusReserva;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Testa o ciclo do job (busca as reservas vencidas e entrega cada uma ao service transacional) com
 * um relógio fixo: o "agora" é sempre 15/12/2026 às 12h, sem depender da hora real.
 */
@ExtendWith(MockitoExtension.class)
class FinalizaReservasVencidasServiceImplTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 12, 15, 12, 0);
    private static final int TAMANHO_LOTE = 10;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ReservaService reservaService;

    private FinalizaReservasVencidasServiceImpl servico;

    @BeforeEach
    void criaOServicoComRelogioFixo() {
        ZoneId fuso = ZoneId.systemDefault();
        Clock relogioFixo = Clock.fixed(AGORA.atZone(fuso).toInstant(), fuso);
        servico = new FinalizaReservasVencidasServiceImpl(reservaRepository, reservaService, relogioFixo);
        // O tamanho do lote vem do application.yml (@Value), que o Mockito não preenche.
        ReflectionTestUtils.setField(servico, "tamanhoLote", TAMANHO_LOTE);
    }

    @Test
    void buscaAtivasEPendentesVencidasComOAgoraDoRelogioEOTamanhoDoLote() {
        servico.processaReservasVencidas();

        verify(reservaRepository).buscaIdReservaVencidaPorStatus(AGORA, StatusReserva.ATIVO, TAMANHO_LOTE);
        verify(reservaRepository).buscaIdReservaVencidaPorStatus(AGORA, StatusReserva.PENDENTE, TAMANHO_LOTE);
    }

    @Test
    void entregaCadaReservaVencidaAoServiceComOMesmoAgora() {
        when(reservaRepository.buscaIdReservaVencidaPorStatus(AGORA, StatusReserva.ATIVO, TAMANHO_LOTE))
                .thenReturn(List.of(1L, 2L));
        when(reservaRepository.buscaIdReservaVencidaPorStatus(AGORA, StatusReserva.PENDENTE, TAMANHO_LOTE))
                .thenReturn(List.of(3L));

        servico.processaReservasVencidas();

        verify(reservaService).processaReservaVencida(1L, AGORA);
        verify(reservaService).processaReservaVencida(2L, AGORA);
        verify(reservaService).processaReservaVencida(3L, AGORA);
    }

    @Test
    void naoChamaOServiceQuandoNaoHaReservaVencida() {
        servico.processaReservasVencidas();

        verifyNoInteractions(reservaService);
    }

    @Test
    void continuaProcessandoAsDemaisQuandoUmaReservaFalha() {
        when(reservaRepository.buscaIdReservaVencidaPorStatus(AGORA, StatusReserva.ATIVO, TAMANHO_LOTE))
                .thenReturn(List.of(1L, 2L));
        when(reservaRepository.buscaIdReservaVencidaPorStatus(AGORA, StatusReserva.PENDENTE, TAMANHO_LOTE))
                .thenReturn(List.of(3L));
        doThrow(new NegocioException(CodigoErro.RESERVA_NAO_ENCONTRADA, "1"))
                .when(reservaService)
                .processaReservaVencida(1L, AGORA);

        // A falha da reserva 1 não pode derrubar o ciclo: as reservas 2 e 3 ainda são processadas.
        assertThatCode(() -> servico.processaReservasVencidas()).doesNotThrowAnyException();

        verify(reservaService).processaReservaVencida(2L, AGORA);
        verify(reservaService).processaReservaVencida(3L, AGORA);
    }
}
