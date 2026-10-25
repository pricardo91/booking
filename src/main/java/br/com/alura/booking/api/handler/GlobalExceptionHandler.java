package br.com.alura.booking.api.handler;

import br.com.alura.booking.exception.CategoriaErro;
import br.com.alura.booking.exception.NegocioException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduz as exceções da aplicação em respostas HTTP, no formato padrão ProblemDetail (RFC 9457).
 * O domínio não conhece HTTP: ele lança {@link NegocioException} com um código de erro, e é aqui que
 * a categoria do erro ganha o seu status e que o código vira texto, buscado no messages.properties.
 *
 * <p>Estende {@link ResponseEntityExceptionHandler} para os erros do próprio Spring MVC (JSON
 * malformado, método não permitido, rota inexistente) continuarem com o status correto, em vez de
 * caírem no tratamento genérico de erro interno.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    static final String TITULO_DADOS_INVALIDOS = "erro.titulo.dados-invalidos";
    static final String DADOS_INVALIDOS = "erro.dados-invalidos";
    static final String TITULO_CONFLITO_DE_DADOS = "erro.titulo.conflito-de-dados";
    static final String CONFLITO_DE_DADOS = "erro.conflito-de-dados";
    static final String TITULO_ERRO_INTERNO = "erro.titulo.erro-interno";
    static final String ERRO_INTERNO = "erro.erro-interno";
    static final String CAMPO_INVALIDO = "erro.campo-invalido";

    /** Chaves usadas só pelo handler; o MensagensDeErroTest confere que todas existem no arquivo. */
    static final List<String> CHAVES_GERAIS = List.of(
            TITULO_DADOS_INVALIDOS,
            DADOS_INVALIDOS,
            TITULO_CONFLITO_DE_DADOS,
            CONFLITO_DE_DADOS,
            TITULO_ERRO_INTERNO,
            ERRO_INTERNO,
            CAMPO_INVALIDO);

    private final MessageSource mensagens;

    /**
     * Erro de negócio: o status vem da categoria do erro, e o texto, do código e dos argumentos.
     */
    @ExceptionHandler(NegocioException.class)
    ProblemDetail tratarNegocio(NegocioException ex) {
        CategoriaErro categoria = ex.getCodigo().getCategoria();
        return problema(
                statusDe(categoria),
                texto(categoria.getChaveTitulo()),
                texto(ex.getCodigo().getChave(), ex.getArgumentos()));
    }

    /**
     * Violação de restrição do banco (ex.: e-mail ou CPF já cadastrado): 409. A mensagem do banco
     * não é repassada ao cliente, porque expõe detalhes internos.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail tratarIntegridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade no banco: {}", ex.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, texto(TITULO_CONFLITO_DE_DADOS), texto(CONFLITO_DE_DADOS));
    }

    /**
     * Erro que ninguém previu: 500. Fica registrado no log com o detalhe completo, mas o cliente
     * recebe só uma mensagem genérica.
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail tratarInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, texto(TITULO_ERRO_INTERNO), texto(ERRO_INTERNO));
    }

    /**
     * Dados da requisição inválidos (@Valid no DTO): 400, com a lista dos campos com problema.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoInvalido(erro.getField(), mensagemDe(erro.getDefaultMessage())))
                .toList();

        ProblemDetail problema =
                problema(HttpStatus.BAD_REQUEST, texto(TITULO_DADOS_INVALIDOS), texto(DADOS_INVALIDOS));
        problema.setProperty("campos", campos);
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    /**
     * Cada categoria de erro de negócio tem o seu status HTTP.
     */
    private static HttpStatus statusDe(CategoriaErro categoria) {
        return switch (categoria) {
            case REGRA_DE_NEGOCIO -> HttpStatus.UNPROCESSABLE_CONTENT;
            case CONFLITO -> HttpStatus.CONFLICT;
            case NAO_ENCONTRADO -> HttpStatus.NOT_FOUND;
        };
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }

    private String texto(String chave, Object... argumentos) {
        return mensagens.getMessage(chave, argumentos, LocaleContextHolder.getLocale());
    }

    private String mensagemDe(String mensagem) {
        return mensagem != null ? mensagem : texto(CAMPO_INVALIDO);
    }

    /**
     * Detalhe de um campo inválido na entrada da API.
     */
    record CampoInvalido(String campo, String mensagem) {}
}
