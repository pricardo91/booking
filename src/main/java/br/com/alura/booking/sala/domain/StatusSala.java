package br.com.alura.booking.sala.domain;

/**
 * Disponibilidade de uma sala para novas reservas. É um enum, e não um boolean, para permitir novos
 * estados no futuro (ex.: EM_MANUTENCAO) sem mudar o tipo do campo.
 */
public enum StatusSala {
    ATIVA, // Sala ativa, disponivel para reserva
    INATIVA // Sala inativa, indisponivel para reserva
}
