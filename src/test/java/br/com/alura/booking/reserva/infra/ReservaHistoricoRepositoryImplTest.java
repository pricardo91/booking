package br.com.alura.booking.reserva.infra;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.alura.booking.PostgresTesteConfig;
import br.com.alura.booking.reserva.domain.MotivoHistorico;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.ReservaHistorico;
import br.com.alura.booking.reserva.domain.StatusReserva;
import br.com.alura.booking.sala.domain.Sala;
import br.com.alura.booking.usuario.domain.Usuario;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

/**
 * Testa o histórico de status contra um Postgres real (Testcontainers): gravação, leitura só das
 * linhas da reserva pedida e ordem cronológica.
 *
 * <p>Cenário gravado antes de cada teste (dia 01/12/2026): duas reservas PENDENTE da Maria, uma na
 * Sala Azul (a "reserva") e outra na Sala Verde (a "outra reserva").
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresTesteConfig.class, ReservaHistoricoRepositoryImpl.class})
class ReservaHistoricoRepositoryImplTest {

    private static final LocalDate DIA = LocalDate.of(2026, 12, 1);

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ReservaHistoricoRepositoryImpl repositorio;

    private Reserva reserva;
    private Reserva outraReserva;

    @BeforeEach
    void gravaCenario() {
        Sala salaAzul = em.persist(new Sala("Sala Azul", 10));
        Sala salaVerde = em.persist(new Sala("Sala Verde", 8));
        Usuario maria = em.persist(new Usuario("Maria Souza", "maria@exemplo.com", "52998224725"));

        reserva = em.persist(new Reserva(maria, salaAzul, hora(9), hora(10)));
        outraReserva = em.persist(new Reserva(maria, salaVerde, hora(9), hora(10)));
        em.flush();
    }

    @Test
    void gravaEListaOsCamposDoRegistro() {
        repositorio.registraReservaHistorico(new ReservaHistorico(
                reserva, StatusReserva.PENDENTE, StatusReserva.ATIVO, MotivoHistorico.CONFIRMACAO));
        em.clear();

        List<ReservaHistorico> historico = repositorio.listaReservaHistorico(reserva.getId());

        assertThat(historico).singleElement().satisfies(registro -> {
            assertThat(registro.getReserva().getId()).isEqualTo(reserva.getId());
            assertThat(registro.getStatusAnterior()).isEqualTo(StatusReserva.PENDENTE);
            assertThat(registro.getStatusNovo()).isEqualTo(StatusReserva.ATIVO);
            assertThat(registro.getMotivo()).isEqualTo(MotivoHistorico.CONFIRMACAO);
            assertThat(registro.getCriadoEm()).isNotNull();
        });
    }

    @Test
    void listaSoOsRegistrosDaReservaPedida() {
        ReservaHistorico daReserva =
                grava(reserva, StatusReserva.PENDENTE, StatusReserva.ATIVO, MotivoHistorico.CONFIRMACAO);
        grava(outraReserva, StatusReserva.PENDENTE, StatusReserva.CANCELADO, MotivoHistorico.CANCELAMENTO_PELO_USUARIO);
        em.clear();

        List<ReservaHistorico> historico = repositorio.listaReservaHistorico(reserva.getId());

        assertThat(historico).extracting(ReservaHistorico::getId).containsExactly(daReserva.getId());
    }

    @Test
    void devolveListaVaziaQuandoAReservaNaoTemHistorico() {
        assertThat(repositorio.listaReservaHistorico(reserva.getId())).isEmpty();
    }

    @Test
    void devolveListaVaziaParaReservaInexistenteSemLancarErro() {
        // Conferir se a reserva existe é papel do service; o repositório só consulta o histórico.
        assertThat(repositorio.listaReservaHistorico(999_999L)).isEmpty();
    }

    @Test
    void ordenaPeloMomentoDaMudancaMesmoQuandoAGravacaoFoiEmOutraOrdem() {
        ReservaHistorico gravadoPrimeiro =
                grava(reserva, StatusReserva.PENDENTE, StatusReserva.ATIVO, MotivoHistorico.CONFIRMACAO);
        ReservaHistorico gravadoDepois =
                grava(reserva, StatusReserva.ATIVO, StatusReserva.CANCELADO, MotivoHistorico.CANCELAMENTO_PELO_USUARIO);
        // O registro gravado primeiro passa a ter o instante mais recente: a ordem do banco (por id) não pode valer.
        defineCriadoEm(gravadoPrimeiro, hora(12));
        defineCriadoEm(gravadoDepois, hora(11));
        em.clear();

        List<ReservaHistorico> historico = repositorio.listaReservaHistorico(reserva.getId());

        assertThat(historico)
                .extracting(ReservaHistorico::getId)
                .containsExactly(gravadoDepois.getId(), gravadoPrimeiro.getId());
    }

    @Test
    void desempataPeloIdQuandoOsRegistrosTemOMesmoInstante() {
        ReservaHistorico primeiro =
                grava(reserva, StatusReserva.PENDENTE, StatusReserva.ATIVO, MotivoHistorico.CONFIRMACAO);
        ReservaHistorico segundo =
                grava(reserva, StatusReserva.ATIVO, StatusReserva.CANCELADO, MotivoHistorico.CANCELAMENTO_PELO_USUARIO);
        defineCriadoEm(primeiro, hora(11));
        defineCriadoEm(segundo, hora(11));
        em.clear();

        List<ReservaHistorico> historico = repositorio.listaReservaHistorico(reserva.getId());

        assertThat(historico).extracting(ReservaHistorico::getId).containsExactly(primeiro.getId(), segundo.getId());
    }

    /** Grava um registro e esvazia o cache do JPA no fim do teste que chamar {@code em.clear()}. */
    private ReservaHistorico grava(
            Reserva daReserva, StatusReserva anterior, StatusReserva novo, MotivoHistorico motivo) {
        ReservaHistorico registro = em.persist(new ReservaHistorico(daReserva, anterior, novo, motivo));
        em.flush();
        return registro;
    }

    /** O instante é preenchido pelo sistema ao gravar; o SQL direto permite controlá-lo no teste. */
    private void defineCriadoEm(ReservaHistorico registro, LocalDateTime instante) {
        em.getEntityManager()
                .createNativeQuery("update reserva_historico set criado_em = :instante where id_historico = :id")
                .setParameter("instante", instante)
                .setParameter("id", registro.getId())
                .executeUpdate();
    }

    private static LocalDateTime hora(int hora) {
        return DIA.atTime(hora, 0);
    }
}
