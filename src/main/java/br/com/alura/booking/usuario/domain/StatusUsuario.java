package br.com.alura.booking.usuario.domain;

/**
 * Situação de um usuário para novas reservas. É um enum, e não um boolean, para permitir novos
 * estados no futuro (ex.: BLOQUEADO) sem mudar o tipo do campo.
 */
public enum StatusUsuario {
    ATIVO, // Usuário ativo, pode fazer reservas
    INATIVO // Usuário inativo, não pode fazer reservas novas; o histórico é mantido
}
