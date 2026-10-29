package br.com.alura.booking.sala.application;

import br.com.alura.booking.sala.domain.Sala;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SalaRepository {

    Sala salva(Sala sala);

    boolean existeSalaPorNome(String nome);

    Sala buscaSalaPorId(long id);

    Page<Sala> listaSalas(Pageable pageable);

    boolean existeOutraSalaPorNome(String nome, long id);
}
