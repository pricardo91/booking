package br.com.alura.booking.shared.exception;

import lombok.Getter;

/**
 * Erro de negócio. Carrega um {@link CodigoErro} (que diz qual erro foi) e os argumentos que
 * completam o texto da mensagem. O texto em si não é resolvido aqui: é o handler da API que o busca
 * no messages.properties.
 *
 * <p>Não conhece HTTP. O {@code getMessage()} devolve só a chave do erro, útil em logs.
 */
@Getter
public class NegocioException extends RuntimeException {

    private final CodigoErro codigo;
    private final transient Object[] argumentos;

    /**
     * @param codigo qual erro aconteceu
     * @param argumentos valores que preenchem os parâmetros ({0}, {1}...) do texto, se houver
     */
    public NegocioException(CodigoErro codigo, Object... argumentos) {
        super(codigo.getChave());
        this.codigo = codigo;
        this.argumentos = argumentos;
    }
}
