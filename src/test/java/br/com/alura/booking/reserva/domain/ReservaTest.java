package br.com.alura.booking.reserva.domain;

import static br.com.alura.booking.shared.exception.ErroAssert.assertErro;
import static org.assertj.core.api.Assertions.assertThat;

import br.com.alura.booking.sala.domain.Sala;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.usuario.domain.Usuario;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

class ReservaTest {

    private static final LocalDate DIA = LocalDate.of(2026, 10, 26);

    private static LocalDateTime as(String hora) {
        return LocalDateTime.of(DIA, LocalTime.parse(hora));
    }

    private static Usuario usuarioValido() {
        return new Usuario("Maria", "maria@email.com", "12345678901");
    }

    private static Sala salaValida() {
        return new Sala("Sala Azul", 10);
    }

    private static Reserva reservaValida() {
        return new Reserva(usuarioValido(), salaValida(), as("10:00"), as("11:00"));
    }

    private static Reserva reserva(String inicio, String fim) {
        return new Reserva(usuarioValido(), salaValida(), as(inicio), as(fim));
    }

    // Para testar sobreposição: as duas reservas precisam estar na MESMA sala.
    private static Reserva reserva(Sala sala, String inicio, String fim) {
        return new Reserva(usuarioValido(), sala, as(inicio), as(fim));
    }

    // ---------- criação ----------

    @Test
    void criaReservaPendenteComOsDadosInformados() {
        Usuario usuario = usuarioValido();
        Sala sala = salaValida();

        Reserva reserva = new Reserva(usuario, sala, as("10:00"), as("11:00"));

        assertThat(reserva.getUsuario()).isSameAs(usuario);
        assertThat(reserva.getSala()).isSameAs(sala);
        assertThat(reserva.getInicio()).isEqualTo(as("10:00"));
        assertThat(reserva.getFim()).isEqualTo(as("11:00"));
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(reserva.getId()).isNull();
    }

    @Test
    void recusaUsuarioNulo() {
        assertErro(
                () -> new Reserva(null, salaValida(), as("10:00"), as("11:00")),
                CodigoErro.CAMPO_OBRIGATORIO,
                "usuário");
    }

    @Test
    void recusaSalaNula() {
        assertErro(
                () -> new Reserva(usuarioValido(), null, as("10:00"), as("11:00")),
                CodigoErro.CAMPO_OBRIGATORIO,
                "sala");
    }

    @Test
    void recusaInicioNulo() {
        assertErro(
                () -> new Reserva(usuarioValido(), salaValida(), null, as("11:00")),
                CodigoErro.CAMPO_OBRIGATORIO,
                "início");
    }

    @Test
    void recusaFimNulo() {
        assertErro(
                () -> new Reserva(usuarioValido(), salaValida(), as("10:00"), null),
                CodigoErro.CAMPO_OBRIGATORIO,
                "fim");
    }

    // ---------- sala ----------

    @Test
    void recusaReservarSalaInativa() {
        Sala sala = salaValida();
        sala.inativar();

        assertErro(() -> new Reserva(usuarioValido(), sala, as("10:00"), as("11:00")), CodigoErro.SALA_INATIVA);
    }

    // ---------- intervalo ----------

    @Test
    void recusaFimAntesDoInicio() {
        assertErro(() -> reserva("11:00", "10:00"), CodigoErro.RESERVA_INTERVALO_INVALIDO);
    }

    @Test
    void recusaFimIgualAoInicio() {
        assertErro(() -> reserva("10:00", "10:00"), CodigoErro.RESERVA_INTERVALO_INVALIDO);
    }

    @Test
    void aceitaFimUmMinutoDepoisDoInicio() {
        Reserva reserva = reserva("10:00", "10:01");

        assertThat(reserva.getFim()).isEqualTo(as("10:01"));
    }

    // ---------- confirmar ----------

    @Test
    void confirmarTornaAReservaAtiva() {
        Reserva reserva = reservaValida();

        reserva.confirmar();

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.ATIVO);
    }

    @Test
    void recusaConfirmarReservaJaConfirmada() {
        Reserva reserva = reservaValida();
        reserva.confirmar();

        assertErro(reserva::confirmar, CodigoErro.RESERVA_SO_PENDENTE_CONFIRMA);
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.ATIVO);
    }

    @Test
    void recusaConfirmarReservaCancelada() {
        Reserva reserva = reservaValida();
        reserva.cancelar();

        assertErro(reserva::confirmar, CodigoErro.RESERVA_SO_PENDENTE_CONFIRMA);
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADO);
    }

    // ---------- cancelar ----------

    @Test
    void cancelarReservaPendenteTornaACancelada() {
        Reserva reserva = reservaValida();

        reserva.cancelar();

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADO);
    }

    @Test
    void cancelarReservaAtivaTornaACancelada() {
        Reserva reserva = reservaValida();
        reserva.confirmar();

        reserva.cancelar();

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADO);
    }

    @Test
    void recusaCancelarReservaJaCancelada() {
        Reserva reserva = reservaValida();
        reserva.cancelar();

        assertErro(reserva::cancelar, CodigoErro.RESERVA_JA_CANCELADA);
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADO);
    }

    @Test
    void recusaCancelarReservaFinalizada() {
        Reserva reserva = reservaFinalizada();

        assertErro(reserva::cancelar, CodigoErro.RESERVA_FINALIZADA_NAO_CANCELA);
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.FINALIZADO);
    }

    // ---------- finalizar ----------

    private static Reserva reservaFinalizada() {
        Reserva reserva = reservaValida();
        reserva.confirmar();
        reserva.finalizar(as("11:00"));
        return reserva;
    }

    @ParameterizedTest(name = "agora = {0}")
    @CsvSource({"11:00", "11:01", "18:00"})
    void finalizarReservaAtivaDepoisQueOHorarioTerminou(String agora) {
        Reserva reserva = reservaValida();
        reserva.confirmar();

        reserva.finalizar(as(agora));

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.FINALIZADO);
    }

    @Test
    void recusaFinalizarAntesDoFimDoHorario() {
        Reserva reserva = reservaValida();
        reserva.confirmar();

        assertErro(() -> reserva.finalizar(as("10:59")), CodigoErro.RESERVA_AINDA_NAO_TERMINOU);
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.ATIVO);
    }

    @Test
    void recusaFinalizarSemInformarOInstante() {
        Reserva reserva = reservaValida();
        reserva.confirmar();

        assertErro(() -> reserva.finalizar(null), CodigoErro.CAMPO_OBRIGATORIO, "instante atual");
    }

    @Test
    void recusaFinalizarReservaPendente() {
        Reserva reserva = reservaValida();

        assertErro(() -> reserva.finalizar(as("12:00")), CodigoErro.RESERVA_SO_ATIVA_FINALIZA);
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.PENDENTE);
    }

    @Test
    void recusaFinalizarReservaCancelada() {
        Reserva reserva = reservaValida();
        reserva.cancelar();

        assertErro(() -> reserva.finalizar(as("12:00")), CodigoErro.RESERVA_SO_ATIVA_FINALIZA);
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADO);
    }

    @Test
    void recusaFinalizarReservaJaFinalizada() {
        Reserva reserva = reservaFinalizada();

        assertErro(() -> reserva.finalizar(as("12:00")), CodigoErro.RESERVA_SO_ATIVA_FINALIZA);
    }

    @Test
    void recusaConfirmarReservaFinalizada() {
        Reserva reserva = reservaFinalizada();

        assertErro(reserva::confirmar, CodigoErro.RESERVA_SO_PENDENTE_CONFIRMA);
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.FINALIZADO);
    }

    // ---------- ocupa a sala ----------

    @Test
    void reservaFinalizadaNaoOcupaASala() {
        assertThat(reservaFinalizada().ocupaSala()).isFalse();
    }

    @Test
    void reservaPendenteNaoOcupaASala() {
        assertThat(reservaValida().ocupaSala()).isFalse();
    }

    @Test
    void reservaAtivaOcupaASala() {
        Reserva reserva = reservaValida();
        reserva.confirmar();

        assertThat(reserva.ocupaSala()).isTrue();
    }

    @Test
    void reservaCanceladaNaoOcupaASala() {
        Reserva reserva = reservaValida();
        reserva.confirmar();
        reserva.cancelar();

        assertThat(reserva.ocupaSala()).isFalse();
    }

    // ---------- sobreposição (intervalo semiaberto [inicio, fim)) ----------

    @ParameterizedTest(name = "{0}-{1} e {2}-{3} => sobrepoe = {4}")
    @CsvSource({
        // fim igual ao início da outra: NÃO sobrepõe
        "10:00, 11:00, 11:00, 12:00, false",
        // começa no meio da outra: sobrepõe
        "10:00, 11:00, 10:30, 11:30, true",
        // uma contida na outra: sobrepõe
        "10:00, 12:00, 10:30, 11:00, true",
        // intervalos idênticos: sobrepõe
        "10:00, 11:00, 10:00, 11:00, true",
        // intervalos distantes: não sobrepõe
        "08:00, 09:00, 10:00, 11:00, false",
        // um minuto de cruzamento: sobrepõe
        "10:00, 11:00, 10:59, 12:00, true"
    })
    void sobrepoeSegueOIntervaloSemiaberto(String inicioA, String fimA, String inicioB, String fimB, boolean esperado) {
        Sala sala = salaValida();
        Reserva a = reserva(sala, inicioA, fimA);
        Reserva b = reserva(sala, inicioB, fimB);

        assertThat(a.sobrepoe(b)).isEqualTo(esperado);
        assertThat(b.sobrepoe(a)).isEqualTo(esperado);
    }

    @Test
    void sobrepoeIgnoraOStatusDasReservas() {
        Sala sala = salaValida();
        Reserva a = reserva(sala, "10:00", "11:00");
        Reserva b = reserva(sala, "10:30", "11:30");
        b.cancelar();

        assertThat(a.sobrepoe(b)).isTrue();
    }

    @Test
    void naoSobrepoeQuandoAsSalasSaoDiferentes() {
        Reserva a = reserva(salaValida(), "10:00", "11:00");
        Reserva b = reserva(salaValida(), "10:00", "11:00");

        assertThat(a.sobrepoe(b)).isFalse();
        assertThat(b.sobrepoe(a)).isFalse();
    }

    @Test
    void sobrepoeQuandoSaoObjetosDiferentesDaMesmaSala() {
        // Simula a mesma sala carregada duas vezes do banco: dois objetos, mesmo id.
        Sala salaA = salaValida();
        Sala salaB = salaValida();
        ReflectionTestUtils.setField(salaA, "id", 1L);
        ReflectionTestUtils.setField(salaB, "id", 1L);
        Reserva a = reserva(salaA, "10:00", "11:00");
        Reserva b = reserva(salaB, "10:30", "11:30");

        assertThat(a.sobrepoe(b)).isTrue();
    }
}
