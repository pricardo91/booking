package br.com.alura.booking.application.repository;

import br.com.alura.booking.domain.Sala;

public interface SalaRepository {

    Sala salva(Sala sala);

    boolean existeSalaPorNome(String nome);

    Sala buscaSalaPorId(long id);
}
