package br.com.alura.booking.reserva.application;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.alura.booking.PostgresTesteConfig;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.StatusReserva;
import br.com.alura.booking.reserva.infra.ReservaHistoricoJPARepository;
import br.com.alura.booking.reserva.infra.ReservaJPARepository;
import br.com.alura.booking.sala.domain.Sala;
import br.com.alura.booking.sala.infra.SalaJPARepository;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import br.com.alura.booking.usuario.domain.Usuario;
import br.com.alura.booking.usuario.infra.UsuarioJPARepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Prova as regras de concorrência da confirmação e do cancelamento:
 *
 * <ul>
 *   <li>se duas reservas PENDENTE disputam o mesmo horário na mesma sala e as duas são confirmadas
 *       ao mesmo tempo, só uma vira ATIVO e a outra recebe o erro de conflito de horário;
 *   <li>se a mesma reserva é confirmada e cancelada ao mesmo tempo, ela termina CANCELADA.
 * </ul>
 *
 * <p>Este teste NÃO usa {@code @Transactional}. A concorrência só existe com transações reais,
 * gravadas de verdade e vistas por duas conexões ao mesmo tempo. Por isso ele grava os dados e os
 * apaga no fim, e repete a disputa várias vezes (cada rodada num horário diferente), porque uma
 * corrida pode passar por sorte numa única tentativa.
 */
@SpringBootTest
@Import(PostgresTesteConfig.class)
class ReservaConfirmacaoConcorrenteTest {

    private static final LocalDate DIA = LocalDate.of(2026, 12, 1);
    private static final int RODADAS = 10;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaJPARepository reservaJPARepository;

    @Autowired
    private ReservaHistoricoJPARepository historicoJPARepository;

    @Autowired
    private SalaJPARepository salaJPARepository;

    @Autowired
    private UsuarioJPARepository usuarioJPARepository;

    private Sala sala;
    private Usuario usuario;

    @BeforeEach
    void gravaSalaEUsuario() {
        sala = salaJPARepository.saveAndFlush(new Sala("Sala Concorrente", 10));
        usuario = usuarioJPARepository.saveAndFlush(new Usuario("Maria Souza", "maria@exemplo.com", "52998224725"));
    }

    @AfterEach
    void apagaTudo() {
        historicoJPARepository.deleteAll();
        reservaJPARepository.deleteAll();
        usuarioJPARepository.deleteAll();
        salaJPARepository.deleteAll();
    }

    @Test
    void duasConfirmacoesSimultaneasNoMesmoHorarioResultamEmUmaSoReservaAtiva() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int rodada = 0; rodada < RODADAS; rodada++) {
                LocalDateTime inicio = DIA.atTime(8 + rodada, 0);
                Long idA = grava(inicio);
                Long idB = grava(inicio);
                CyclicBarrier largada = new CyclicBarrier(2);

                Future<Exception> a = pool.submit(() -> confirma(idA, largada));
                Future<Exception> b = pool.submit(() -> confirma(idB, largada));
                List<Exception> resultados = Arrays.asList(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS));

                assertThat(resultados.stream().filter(Objects::isNull).count())
                        .as("confirmações que deram certo na rodada %d", rodada)
                        .isEqualTo(1);
                assertThat(resultados.stream().filter(Objects::nonNull).toList())
                        .as("erro da confirmação perdedora na rodada %d", rodada)
                        .singleElement()
                        .isInstanceOfSatisfying(
                                NegocioException.class,
                                erro -> assertThat(erro.getCodigo()).isEqualTo(CodigoErro.RESERVA_CONFLITO_HORARIO));
                assertThat(reservasAtivasEm(inicio))
                        .as("reservas ativas no horário da rodada %d", rodada)
                        .isEqualTo(1);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void confirmacaoECancelamentoSimultaneosDaMesmaReservaTerminamCanceladaENuncaAtiva() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int rodada = 0; rodada < RODADAS; rodada++) {
                Long id = grava(DIA.atTime(8 + rodada, 0));
                CyclicBarrier largada = new CyclicBarrier(2);

                Future<Exception> confirmacao = pool.submit(() -> confirma(id, largada));
                Future<Exception> cancelamento = pool.submit(() -> cancela(id, largada));
                Exception erroDaConfirmacao = confirmacao.get(30, TimeUnit.SECONDS);
                Exception erroDoCancelamento = cancelamento.get(30, TimeUnit.SECONDS);

                // Cancelar sempre dá certo, porque PENDENTE e ATIVO podem ser cancelados.
                assertThat(erroDoCancelamento)
                        .as("erro do cancelamento na rodada %d", rodada)
                        .isNull();
                // Se o cancelamento entrou primeiro, a confirmação encontra a reserva CANCELADA e é recusada.
                if (erroDaConfirmacao != null) {
                    assertThat(erroDaConfirmacao)
                            .as("erro da confirmação na rodada %d", rodada)
                            .isInstanceOfSatisfying(
                                    NegocioException.class,
                                    erro -> assertThat(erro.getCodigo())
                                            .isEqualTo(CodigoErro.RESERVA_SO_PENDENTE_CONFIRMA));
                }
                // Sem o lock da sala, os dois leriam PENDENTE e a reserva cancelada poderia voltar como ATIVO.
                assertThat(reservaJPARepository.findById(id).orElseThrow().getStatus())
                        .as("status final da reserva na rodada %d", rodada)
                        .isEqualTo(StatusReserva.CANCELADO);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /** Grava uma reserva PENDENTE de uma hora e devolve o id. */
    private Long grava(LocalDateTime inicio) {
        return reservaJPARepository
                .saveAndFlush(new Reserva(usuario, sala, inicio, inicio.plusHours(1)))
                .getId();
    }

    /**
     * Espera a largada das duas threads e confirma. Devolve o erro que a confirmação lançou, ou null
     * se deu certo.
     */
    private Exception confirma(Long id, CyclicBarrier largada) {
        try {
            largada.await(10, TimeUnit.SECONDS);
            reservaService.confirmaReserva(id);
            return null;
        } catch (Exception erro) {
            return erro;
        }
    }

    /** Igual ao confirma, mas cancela. Devolve o erro lançado, ou null se deu certo. */
    private Exception cancela(Long id, CyclicBarrier largada) {
        try {
            largada.await(10, TimeUnit.SECONDS);
            reservaService.cancelaReserva(id);
            return null;
        } catch (Exception erro) {
            return erro;
        }
    }

    private long reservasAtivasEm(LocalDateTime inicio) {
        return reservaJPARepository.findAll().stream()
                .filter(reserva -> reserva.getStatus() == StatusReserva.ATIVO)
                .filter(reserva -> reserva.getInicio().equals(inicio))
                .count();
    }
}
