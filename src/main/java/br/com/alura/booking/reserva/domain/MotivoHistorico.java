package br.com.alura.booking.reserva.domain;

/**
 * Por que o status de uma reserva mudou. Guardado em cada {@link ReservaHistorico}. Gravado como
 * texto (o nome do valor) numa coluna sem restrição de valores, então um motivo novo pode ser
 * acrescentado aqui sem migration.
 */
public enum MotivoHistorico {
    CONFIRMACAO, // o usuário confirmou a reserva
    CANCELAMENTO_PELO_USUARIO, // o usuário cancelou a reserva
    CONFLITO_COM_RESERVA_CONFIRMADA, // cancelada automaticamente porque outra reserva ganhou o horário
    FINALIZACAO_AUTOMATICA, // finalizada automaticamente porque o fim do horário chegou
    EXPIRACAO_SEM_CONFIRMACAO // cancelada automaticamente porque o horário passou sem a confirmação
}
