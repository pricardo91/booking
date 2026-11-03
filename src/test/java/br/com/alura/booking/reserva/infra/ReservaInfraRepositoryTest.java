package br.com.alura.booking.reserva.infra;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.alura.booking.PostgresTesteConfig;
import br.com.alura.booking.reserva.application.ReservaFiltro;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.StatusReserva;
import br.com.alura.booking.sala.domain.Sala;
import br.com.alura.booking.usuario.domain.Usuario;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Testa a lista de reservas contra um Postgres real (Testcontainers): filtros, ordem e paginação.
 *
 * <p>Cenário gravado antes de cada teste (dia 01/12/2026, uma hora de duração cada):
 *
 * <ul>
 *   <li>9h: Maria, Sala Azul, PENDENTE
 *   <li>10h: Maria, Sala Verde, ATIVO
 *   <li>11h: João, Sala Azul, ATIVO
 *   <li>13h: João, Sala Verde, CANCELADO
 * </ul>
 */
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresTesteConfig.class, ReservaInfraRepository.class})
class ReservaInfraRepositoryTest {

    private static final LocalDate DIA = LocalDate.of(2026, 12, 1);

    /** Mesma paginação que o controller usa: mais recente primeiro. */
    private static final Pageable PADRAO =
            PageRequest.of(0, 15, Sort.by("inicio").descending());

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ReservaInfraRepository repositorio;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Sala salaAzul;
    private Sala salaVerde;
    private Usuario maria;
    private Usuario joao;
    private Reserva pendenteDaMariaNaAzul;
    private Reserva ativaDaMariaNaVerde;
    private Reserva ativaDoJoaoNaAzul;
    private Reserva canceladaDoJoaoNaVerde;

    @BeforeEach
    void gravaCenario() {
        salaAzul = em.persist(new Sala("Sala Azul", 10));
        salaVerde = em.persist(new Sala("Sala Verde", 8));
        maria = em.persist(new Usuario("Maria Souza", "maria@exemplo.com", "52998224725"));
        joao = em.persist(new Usuario("João Lima", "joao@exemplo.com", "11144477735"));

        pendenteDaMariaNaAzul = new Reserva(maria, salaAzul, hora(9), hora(10));

        ativaDaMariaNaVerde = new Reserva(maria, salaVerde, hora(10), hora(11));
        ativaDaMariaNaVerde.confirmar();

        ativaDoJoaoNaAzul = new Reserva(joao, salaAzul, hora(11), hora(12));
        ativaDoJoaoNaAzul.confirmar();

        canceladaDoJoaoNaVerde = new Reserva(joao, salaVerde, hora(13), hora(14));
        canceladaDoJoaoNaVerde.cancelar();

        em.persist(pendenteDaMariaNaAzul);
        em.persist(ativaDaMariaNaVerde);
        em.persist(ativaDoJoaoNaAzul);
        em.persist(canceladaDoJoaoNaVerde);

        // Grava no banco e esvazia o cache do JPA, para as consultas lerem do Postgres de verdade.
        em.flush();
        em.clear();
    }

    @Test
    void listaTodasQuandoNaoHaFiltro() {
        Page<Reserva> pagina = repositorio.listaReservas(filtro(null, null, null), PADRAO);

        assertThat(ids(pagina))
                .containsExactlyInAnyOrder(
                        pendenteDaMariaNaAzul.getId(),
                        ativaDaMariaNaVerde.getId(),
                        ativaDoJoaoNaAzul.getId(),
                        canceladaDoJoaoNaVerde.getId());
    }

    @Test
    void filtraPorStatus() {
        Page<Reserva> pagina = repositorio.listaReservas(filtro(StatusReserva.ATIVO, null, null), PADRAO);

        assertThat(ids(pagina)).containsExactlyInAnyOrder(ativaDaMariaNaVerde.getId(), ativaDoJoaoNaAzul.getId());
    }

    @Test
    void filtraPorSala() {
        Page<Reserva> pagina = repositorio.listaReservas(filtro(null, salaAzul.getId(), null), PADRAO);

        assertThat(ids(pagina)).containsExactlyInAnyOrder(pendenteDaMariaNaAzul.getId(), ativaDoJoaoNaAzul.getId());
    }

    @Test
    void filtraPorUsuario() {
        Page<Reserva> pagina = repositorio.listaReservas(filtro(null, null, maria.getId()), PADRAO);

        assertThat(ids(pagina)).containsExactlyInAnyOrder(pendenteDaMariaNaAzul.getId(), ativaDaMariaNaVerde.getId());
    }

    @Test
    void combinaStatusESala() {
        Page<Reserva> pagina = repositorio.listaReservas(filtro(StatusReserva.ATIVO, salaAzul.getId(), null), PADRAO);

        assertThat(ids(pagina)).containsExactly(ativaDoJoaoNaAzul.getId());
    }

    @Test
    void combinaOsTresFiltros() {
        Page<Reserva> pagina =
                repositorio.listaReservas(filtro(StatusReserva.PENDENTE, salaAzul.getId(), maria.getId()), PADRAO);

        assertThat(ids(pagina)).containsExactly(pendenteDaMariaNaAzul.getId());
    }

    @Test
    void devolveVazioQuandoNenhumaReservaAtendeATodosOsFiltros() {
        // Cada filtro sozinho acha reservas; juntos, nenhuma: a Maria na Sala Azul só tem a PENDENTE.
        Page<Reserva> pagina =
                repositorio.listaReservas(filtro(StatusReserva.ATIVO, salaAzul.getId(), maria.getId()), PADRAO);

        assertThat(pagina.getContent()).isEmpty();
        assertThat(pagina.getTotalElements()).isZero();
    }

    @Test
    void devolveVazioQuandoNenhumaReservaTemOStatus() {
        Page<Reserva> pagina = repositorio.listaReservas(filtro(StatusReserva.FINALIZADO, null, null), PADRAO);

        assertThat(pagina.getContent()).isEmpty();
    }

    @Test
    void ordenaDaReservaMaisRecenteParaAMaisAntiga() {
        Page<Reserva> pagina = repositorio.listaReservas(filtro(null, null, null), PADRAO);

        assertThat(ids(pagina))
                .containsExactly(
                        canceladaDoJoaoNaVerde.getId(),
                        ativaDoJoaoNaAzul.getId(),
                        ativaDaMariaNaVerde.getId(),
                        pendenteDaMariaNaAzul.getId());
    }

    @Test
    void paginaOResultado() {
        Sort maisRecentePrimeiro = Sort.by("inicio").descending();

        Page<Reserva> primeira =
                repositorio.listaReservas(filtro(null, null, null), PageRequest.of(0, 2, maisRecentePrimeiro));
        Page<Reserva> segunda =
                repositorio.listaReservas(filtro(null, null, null), PageRequest.of(1, 2, maisRecentePrimeiro));

        assertThat(primeira.getTotalElements()).isEqualTo(4);
        assertThat(primeira.getTotalPages()).isEqualTo(2);
        assertThat(ids(primeira)).containsExactly(canceladaDoJoaoNaVerde.getId(), ativaDoJoaoNaAzul.getId());
        assertThat(ids(segunda)).containsExactly(ativaDaMariaNaVerde.getId(), pendenteDaMariaNaAzul.getId());
    }

    @Test
    void carregaUsuarioESalaNaMesmaConsulta() {
        Statistics estatisticas =
                entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();

        Page<Reserva> pagina = repositorio.listaReservas(filtro(null, null, null), PADRAO);

        // Ler o nome do usuário e da sala de cada reserva, como o ReservaResumoResponse faz. Sem o
        // @EntityGraph, cada leitura dispararia uma consulta nova (o problema do N+1).
        assertThat(pagina.getContent())
                .extracting(reserva -> reserva.getUsuario().getNome())
                .containsExactly("João Lima", "João Lima", "Maria Souza", "Maria Souza");
        assertThat(pagina.getContent())
                .extracting(reserva -> reserva.getSala().getNome())
                .containsExactly("Sala Verde", "Sala Azul", "Sala Verde", "Sala Azul");
        assertThat(estatisticas.getPrepareStatementCount()).isEqualTo(1);
    }

    // ---------- existeReservaAtivaSobreposta ----------
    // A Sala Verde tem a reserva ATIVO da Maria das 10h às 11h. Cada teste grava uma candidata nova.

    @Test
    void detectaReservaAtivaQueCruzaOHorario() {
        Reserva candidata = grava(joao, salaVerde, hora(10).plusMinutes(30), hora(11).plusMinutes(30));

        assertThat(repositorio.existeReservaAtivaSobreposta(candidata)).isTrue();
    }

    @Test
    void detectaReservaAtivaQueContemOHorario() {
        Reserva candidata = grava(joao, salaVerde, hora(10).plusMinutes(15), hora(10).plusMinutes(45));

        assertThat(repositorio.existeReservaAtivaSobreposta(candidata)).isTrue();
    }

    @Test
    void detectaReservaAtivaContidaNoHorario() {
        Reserva candidata = grava(joao, salaVerde, hora(9).plusMinutes(30), hora(12));

        assertThat(repositorio.existeReservaAtivaSobreposta(candidata)).isTrue();
    }

    @Test
    void naoConflitaQuandoUmaTerminaOndeAOutraComeca() {
        Reserva candidata = grava(joao, salaVerde, hora(9), hora(10));

        assertThat(repositorio.existeReservaAtivaSobreposta(candidata)).isFalse();
    }

    @Test
    void naoConflitaQuandoUmaComecaOndeAOutraTermina() {
        Reserva candidata = grava(joao, salaVerde, hora(11), hora(12));

        assertThat(repositorio.existeReservaAtivaSobreposta(candidata)).isFalse();
    }

    @Test
    void naoConflitaEmOutraSala() {
        Sala salaAmarela = em.persist(new Sala("Sala Amarela", 6));
        Reserva candidata = grava(joao, salaAmarela, hora(10), hora(11));

        assertThat(repositorio.existeReservaAtivaSobreposta(candidata)).isFalse();
    }

    @Test
    void ignoraReservaCancelada() {
        Reserva candidata = grava(maria, salaVerde, hora(13).plusMinutes(30), hora(14).plusMinutes(30));

        assertThat(repositorio.existeReservaAtivaSobreposta(candidata)).isFalse();
    }

    @Test
    void ignoraReservaPendente() {
        // A Sala Azul tem uma PENDENTE das 9h às 10h, mas nenhuma ATIVO nesse horário.
        Reserva candidata = grava(joao, salaAzul, hora(9).plusMinutes(30), hora(10).plusMinutes(30));

        assertThat(repositorio.existeReservaAtivaSobreposta(candidata)).isFalse();
    }

    @Test
    void naoContaAPropriaReservaComoConflito() {
        assertThat(repositorio.existeReservaAtivaSobreposta(ativaDaMariaNaVerde))
                .isFalse();
    }

    // ---------- listaPendentesSobrepostas ----------
    // A Sala Azul tem a PENDENTE da Maria das 9h às 10h e a ATIVO do João das 11h às 12h.

    @Test
    void listaAsPendentesQueCruzamOHorario() {
        Reserva candidata = grava(joao, salaAzul, hora(9).plusMinutes(30), hora(10).plusMinutes(30));

        assertThat(idsDe(repositorio.listaPendentesSobrepostas(candidata)))
                .containsExactly(pendenteDaMariaNaAzul.getId());
    }

    @Test
    void listaPendentesIgnoraAsReservasAtivasEAsCanceladas() {
        // Na Sala Verde há uma ATIVO às 10h e uma CANCELADO às 13h, e nenhuma PENDENTE.
        Reserva candidata = grava(joao, salaVerde, hora(9), hora(14));

        assertThat(repositorio.listaPendentesSobrepostas(candidata)).isEmpty();
    }

    @Test
    void listaPendentesNaoIncluiAPropriaReserva() {
        assertThat(repositorio.listaPendentesSobrepostas(pendenteDaMariaNaAzul)).isEmpty();
    }

    @Test
    void listaPendentesIgnoraAsDeOutraSala() {
        // A PENDENTE da Sala Azul é das 9h às 10h, o mesmo horário desta candidata na Sala Verde.
        Reserva candidata = grava(joao, salaVerde, hora(9), hora(10));

        assertThat(repositorio.listaPendentesSobrepostas(candidata)).isEmpty();
    }

    /** Grava uma reserva PENDENTE e esvazia o cache do JPA, para as consultas lerem do banco. */
    private Reserva grava(Usuario usuario, Sala sala, LocalDateTime inicio, LocalDateTime fim) {
        Reserva reserva = em.persist(new Reserva(usuario, sala, inicio, fim));
        em.flush();
        em.clear();
        return reserva;
    }

    private static List<Long> idsDe(List<Reserva> reservas) {
        return reservas.stream().map(Reserva::getId).toList();
    }

    private static LocalDateTime hora(int hora) {
        return DIA.atTime(hora, 0);
    }

    private static ReservaFiltro filtro(StatusReserva status, Long idSala, Long idUsuario) {
        return new ReservaFiltro(status, idSala, idUsuario);
    }

    private static List<Long> ids(Page<Reserva> pagina) {
        return pagina.getContent().stream().map(Reserva::getId).toList();
    }
}
