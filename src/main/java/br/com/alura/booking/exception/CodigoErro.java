package br.com.alura.booking.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Todos os erros de negócio da aplicação, em um lugar só. Cada código tem uma chave, cujo texto está
 * no arquivo messages.properties, e uma categoria, que define o tipo de resposta.
 *
 * <p>O texto pode ter parâmetros ({0}, {1}...), preenchidos com os argumentos da
 * {@link NegocioException}. Passe números (ids, por exemplo) como texto: o formatador de mensagens
 * insere separador de milhar em números (1.500), o que não é desejado em um id.
 *
 * <p>Ao criar um código novo, crie também a sua chave no messages.properties. O teste
 * MensagensDeErroTest falha se alguma chave estiver faltando.
 */
@Getter
@RequiredArgsConstructor
public enum CodigoErro {

    /** Argumento: o nome do campo (ex.: "sala"). */
    CAMPO_OBRIGATORIO("campo.obrigatorio", CategoriaErro.REGRA_DE_NEGOCIO),

    SALA_CAPACIDADE_INVALIDA("sala.capacidade.invalida", CategoriaErro.REGRA_DE_NEGOCIO),
    SALA_INATIVA("sala.inativa", CategoriaErro.REGRA_DE_NEGOCIO),
    SALA_JA_ATIVA("sala.ja.ativa", CategoriaErro.CONFLITO),
    SALA_JA_INATIVA("sala.ja.inativa", CategoriaErro.CONFLITO),

    /** Argumento: o id da sala. */
    SALA_NAO_ENCONTRADA("sala.nao.encontrada", CategoriaErro.NAO_ENCONTRADO),

    /** Argumento: o nome da sala. */
    SALA_NOME_DUPLICADO("sala.nome.duplicado", CategoriaErro.CONFLITO),

    RESERVA_INTERVALO_INVALIDO("reserva.intervalo.invalido", CategoriaErro.REGRA_DE_NEGOCIO),
    RESERVA_AINDA_NAO_TERMINOU("reserva.ainda.nao.terminou", CategoriaErro.REGRA_DE_NEGOCIO),
    RESERVA_SO_PENDENTE_CONFIRMA("reserva.so.pendente.confirma", CategoriaErro.CONFLITO),
    RESERVA_SO_ATIVA_FINALIZA("reserva.so.ativa.finaliza", CategoriaErro.CONFLITO),
    RESERVA_JA_CANCELADA("reserva.ja.cancelada", CategoriaErro.CONFLITO),
    RESERVA_FINALIZADA_NAO_CANCELA("reserva.finalizada.nao.cancela", CategoriaErro.CONFLITO),
    RESERVA_CONFLITO_HORARIO("reserva.conflito.horario", CategoriaErro.CONFLITO),

    /** Argumento: o id da reserva. */
    RESERVA_NAO_ENCONTRADA("reserva.nao.encontrada", CategoriaErro.NAO_ENCONTRADO),

    /** Argumento: o id do usuário. */
    USUARIO_NAO_ENCONTRADO("usuario.nao.encontrado", CategoriaErro.NAO_ENCONTRADO),

    /** Argumento: o e-mail. */
    USUARIO_EMAIL_DUPLICADO("usuario.email.duplicado", CategoriaErro.CONFLITO),

    /** Sem argumento: o CPF é dado pessoal e não deve aparecer na resposta. */
    USUARIO_CPF_DUPLICADO("usuario.cpf.duplicado", CategoriaErro.CONFLITO),

    USUARIO_JA_ATIVO("usuario.ja.ativo", CategoriaErro.CONFLITO),

    USUARIO_JA_INATIVO("usuario.ja.inativo", CategoriaErro.CONFLITO);

    /** Chave, no messages.properties, do texto deste erro. */
    private final String chave;

    /** Tipo do erro, que o handler traduz em status HTTP. */
    private final CategoriaErro categoria;
}
