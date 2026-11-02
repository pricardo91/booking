package br.com.alura.booking.reserva.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.alura.booking.PostgresTesteConfig;
import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.sala.domain.Sala;
import br.com.alura.booking.usuario.domain.Usuario;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testa os endpoints de reserva de ponta a ponta: requisição HTTP, validação, service, banco
 * (Postgres real via Testcontainers) e resposta. Cada teste roda numa transação desfeita no fim.
 *
 * <p>Cenário gravado antes de cada teste (dia 01/12/2026, uma hora cada): a Maria tem uma reserva
 * PENDENTE na Sala Azul às 9h e uma ATIVO na Sala Verde às 10h.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTesteConfig.class)
@Transactional
class ReservaControllerTest {

    private static final LocalDate DIA = LocalDate.of(2026, 12, 1);
    private static final String URL = "/api/v1/reservas";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    private Sala salaAzul;
    private Sala salaVerde;
    private Usuario maria;
    private Reserva pendenteNaAzul;
    private Reserva ativaNaVerde;

    @BeforeEach
    void gravaCenario() {
        salaAzul = new Sala("Sala Azul", 10);
        salaVerde = new Sala("Sala Verde", 8);
        maria = new Usuario("Maria Souza", "maria@exemplo.com", "52998224725");
        em.persist(salaAzul);
        em.persist(salaVerde);
        em.persist(maria);

        pendenteNaAzul = new Reserva(maria, salaAzul, hora(9), hora(10));
        ativaNaVerde = new Reserva(maria, salaVerde, hora(10), hora(11));
        ativaNaVerde.confirmar();
        em.persist(pendenteNaAzul);
        em.persist(ativaNaVerde);
        em.flush();
        em.clear();
    }

    // ---------- GET /api/v1/reservas ----------

    @Test
    void listaAsReservasDaMaisRecenteParaAMaisAntiga() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.paginaAtual").value(0))
                .andExpect(jsonPath("$.totalPaginas").value(1))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(ativaNaVerde.getId()))
                .andExpect(jsonPath("$.content[0].salaNome").value("Sala Verde"))
                .andExpect(jsonPath("$.content[0].usuarioNome").value("Maria Souza"))
                .andExpect(jsonPath("$.content[0].status").value("ATIVO"))
                .andExpect(jsonPath("$.content[1].id").value(pendenteNaAzul.getId()));
    }

    @Test
    void filtraPorStatus() throws Exception {
        mockMvc.perform(get(URL).param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(ativaNaVerde.getId()));
    }

    @Test
    void filtraPorSalaEUsuario() throws Exception {
        mockMvc.perform(get(URL).param("idSala", String.valueOf(salaAzul.getId()))
                        .param("idUsuario", String.valueOf(maria.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(pendenteNaAzul.getId()));
    }

    @Test
    void devolvePaginaVaziaQuandoNenhumaReservaAtendeOFiltro() throws Exception {
        mockMvc.perform(get(URL).param("status", "FINALIZADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"size=0", "size=101", "page=-1", "status=INVALIDO", "idSala=abc", "idUsuario=abc"})
    void recusaParametrosInvalidosDaLista(String parametro) throws Exception {
        mockMvc.perform(get(URL + "?" + parametro))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ---------- GET /api/v1/reservas/{id} ----------

    @Test
    void buscaReservaPorId() throws Exception {
        mockMvc.perform(get(URL + "/" + ativaNaVerde.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ativaNaVerde.getId()))
                .andExpect(jsonPath("$.usuarioNome").value("Maria Souza"))
                .andExpect(jsonPath("$.salaNome").value("Sala Verde"))
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void buscaReservaInexistenteDevolve404() throws Exception {
        mockMvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(containsString("999999")));
    }

    @Test
    void buscaReservaComIdInvalidoDevolve400() throws Exception {
        mockMvc.perform(get(URL + "/abc")).andExpect(status().isBadRequest());
    }

    // ---------- POST /api/v1/reservas ----------

    @Test
    void cadastraReservaPendente() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(maria.getId(), salaAzul.getId(), "15/12/2026 10:00:00", "15/12/2026 11:00:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.usuarioNome").value("Maria Souza"))
                .andExpect(jsonPath("$.salaNome").value("Sala Azul"));
    }

    @Test
    void cadastraReservaDeUsuarioInexistenteDevolve404() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(999999L, salaAzul.getId(), "15/12/2026 10:00:00", "15/12/2026 11:00:00")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void cadastraReservaComIntervaloInvalidoDevolve422() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(maria.getId(), salaAzul.getId(), "15/12/2026 10:00:00", "15/12/2026 10:00:00")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void cadastraReservaSemCamposObrigatoriosDevolve400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private static LocalDateTime hora(int hora) {
        return DIA.atTime(hora, 0);
    }

    private static String corpo(Long usuarioId, Long salaId, String inicio, String fim) {
        return """
                {"usuarioId": %d, "salaId": %d, "inicio": "%s", "fim": "%s"}
                """
                .formatted(usuarioId, salaId, inicio, fim);
    }
}
