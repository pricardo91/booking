package br.com.alura.booking.api.handler;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.alura.booking.exception.CodigoErro;
import br.com.alura.booking.exception.NegocioException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(mensagens());

    static ResourceBundleMessageSource mensagens() {
        ResourceBundleMessageSource mensagens = new ResourceBundleMessageSource();
        mensagens.setBasename("messages");
        mensagens.setDefaultEncoding("UTF-8");
        mensagens.setFallbackToSystemLocale(false);
        return mensagens;
    }

    @Test
    void regraDeNegocioViraStatus422ComOTextoDoCodigo() {
        ProblemDetail problema = handler.tratarNegocio(new NegocioException(CodigoErro.SALA_INATIVA));

        assertThat(problema.getStatus()).isEqualTo(422);
        assertThat(problema.getTitle()).isEqualTo("Regra de negócio violada");
        assertThat(problema.getDetail()).isEqualTo("Não é possível reservar uma sala inativa.");
    }

    @Test
    void conflitoViraStatus409ComOTextoDoCodigo() {
        ProblemDetail problema = handler.tratarNegocio(new NegocioException(CodigoErro.RESERVA_JA_CANCELADA));

        assertThat(problema.getStatus()).isEqualTo(409);
        assertThat(problema.getTitle()).isEqualTo("Conflito de estado");
        assertThat(problema.getDetail()).isEqualTo("A reserva já foi cancelada.");
    }

    @Test
    void naoEncontradoViraStatus404ComOArgumentoNoTexto() {
        // O id vai como texto para o formatador não inserir separador de milhar (1.500).
        ProblemDetail problema = handler.tratarNegocio(new NegocioException(CodigoErro.SALA_NAO_ENCONTRADA, "1500"));

        assertThat(problema.getStatus()).isEqualTo(404);
        assertThat(problema.getTitle()).isEqualTo("Recurso não encontrado");
        assertThat(problema.getDetail()).isEqualTo("Sala com ID 1500 não foi encontrada.");
    }

    @Test
    void campoObrigatorioCitaONomeDoCampo() {
        ProblemDetail problema = handler.tratarNegocio(new NegocioException(CodigoErro.CAMPO_OBRIGATORIO, "sala"));

        assertThat(problema.getDetail()).isEqualTo("O campo sala é obrigatório.");
    }

    @Test
    void violacaoDeIntegridadeViraStatus409SemExporOErroDoBanco() {
        DataIntegrityViolationException erro =
                new DataIntegrityViolationException("duplicate key value violates unique constraint uk_email");

        ProblemDetail problema = handler.tratarIntegridade(erro);

        assertThat(problema.getStatus()).isEqualTo(409);
        assertThat(problema.getTitle()).isEqualTo("Conflito de dados");
        assertThat(problema.getDetail()).doesNotContain("duplicate").doesNotContain("uk_email");
    }

    @Test
    void erroInesperadoViraStatus500SemExporAMensagemOriginal() {
        ProblemDetail problema = handler.tratarInesperado(new RuntimeException("senha do banco: 123"));

        assertThat(problema.getStatus()).isEqualTo(500);
        assertThat(problema.getTitle()).isEqualTo("Erro interno");
        assertThat(problema.getDetail()).doesNotContain("senha");
    }

    @Test
    void dadosInvalidosViramStatus400ComAListaDeCampos() throws Exception {
        BeanPropertyBindingResult resultado = new BeanPropertyBindingResult(new Object(), "sala");
        resultado.addError(new FieldError("sala", "nome", "não pode ser vazio"));
        resultado.addError(new FieldError("sala", "capacidade", "deve ser maior que 0"));
        Method metodo = GlobalExceptionHandlerTest.class.getDeclaredMethod("qualquer", String.class);
        MethodArgumentNotValidException erro =
                new MethodArgumentNotValidException(new MethodParameter(metodo, 0), resultado);

        ResponseEntity<Object> resposta = handler.handleMethodArgumentNotValid(
                erro, new HttpHeaders(), HttpStatus.BAD_REQUEST, new ServletWebRequest(new MockHttpServletRequest()));

        ProblemDetail problema = (ProblemDetail) resposta.getBody();
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(problema).isNotNull();
        assertThat(problema.getTitle()).isEqualTo("Dados inválidos");
        assertThat(problema.getDetail()).isEqualTo("Um ou mais campos estão inválidos.");
        assertThat(problema.getProperties()).containsKey("campos");
        assertThat(problema.getProperties().get("campos"))
                .isEqualTo(List.of(
                        new GlobalExceptionHandler.CampoInvalido("nome", "não pode ser vazio"),
                        new GlobalExceptionHandler.CampoInvalido("capacidade", "deve ser maior que 0")));
    }

    // Método só para fabricar o MethodParameter exigido pelo construtor da exceção.
    @SuppressWarnings("unused")
    private void qualquer(String parametro) {}
}
