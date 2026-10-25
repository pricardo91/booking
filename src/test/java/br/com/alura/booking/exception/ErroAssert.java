package br.com.alura.booking.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

/**
 * Auxiliar dos testes: confere que uma ação lança uma {@link NegocioException} com o código (e os
 * argumentos) esperados. Conferir o código, e não o texto, deixa os testes independentes da redação
 * das mensagens.
 */
public final class ErroAssert {

    private ErroAssert() {}

    public static void assertErro(ThrowingCallable acao, CodigoErro esperado, Object... argumentos) {
        assertThatThrownBy(acao).isInstanceOfSatisfying(NegocioException.class, ex -> {
            assertThat(ex.getCodigo()).isEqualTo(esperado);
            assertThat(ex.getArgumentos()).containsExactly(argumentos);
        });
    }
}
