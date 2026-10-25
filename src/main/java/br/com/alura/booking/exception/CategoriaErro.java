package br.com.alura.booking.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A natureza de um erro de negócio. Não é um status HTTP: o domínio e este pacote não conhecem HTTP.
 * Quem traduz a categoria em status é o handler da camada de API (regra de negócio vira 422,
 * conflito vira 409, não encontrado vira 404).
 */
@Getter
@RequiredArgsConstructor
public enum CategoriaErro {

    /** Os dados estão bem formados, mas violam uma regra do negócio. */
    REGRA_DE_NEGOCIO("erro.titulo.regra-de-negocio"),

    /** A operação não é compatível com o estado atual do que já existe. */
    CONFLITO("erro.titulo.conflito"),

    /** O recurso pedido não existe. */
    NAO_ENCONTRADO("erro.titulo.nao-encontrado");

    /** Chave, no messages.properties, do título da resposta desta categoria. */
    private final String chaveTitulo;
}
