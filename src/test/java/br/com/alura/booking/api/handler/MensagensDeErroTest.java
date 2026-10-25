package br.com.alura.booking.api.handler;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.alura.booking.exception.CategoriaErro;
import br.com.alura.booking.exception.CodigoErro;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.MessageSource;

/**
 * Protege o arquivo messages.properties: todo código de erro, toda categoria e toda mensagem geral
 * do handler precisam ter texto. Sem isso, a falta só apareceria em produção, na hora do erro.
 */
class MensagensDeErroTest {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final Object[] ARGUMENTOS = {"x", "y"};

    private final MessageSource mensagens = GlobalExceptionHandlerTest.mensagens();

    @ParameterizedTest
    @EnumSource(CodigoErro.class)
    void todoCodigoDeErroTemTexto(CodigoErro codigo) {
        String texto = mensagens.getMessage(codigo.getChave(), ARGUMENTOS, PT_BR);

        assertThat(texto).isNotBlank().doesNotContain("{");
    }

    @ParameterizedTest
    @EnumSource(CategoriaErro.class)
    void todaCategoriaTemTitulo(CategoriaErro categoria) {
        String titulo = mensagens.getMessage(categoria.getChaveTitulo(), null, PT_BR);

        assertThat(titulo).isNotBlank();
    }

    @ParameterizedTest
    @MethodSource("chavesGeraisDoHandler")
    void todaMensagemGeralDoHandlerTemTexto(String chave) {
        String texto = mensagens.getMessage(chave, null, PT_BR);

        assertThat(texto).isNotBlank();
    }

    static List<String> chavesGeraisDoHandler() {
        return GlobalExceptionHandler.CHAVES_GERAIS;
    }

    @Test
    void asChavesDosCodigosDeErroNaoSeRepetem() {
        Set<String> chaves = Arrays.stream(CodigoErro.values()).map(CodigoErro::getChave).collect(Collectors.toSet());

        assertThat(chaves).hasSize(CodigoErro.values().length);
    }
}
