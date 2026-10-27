package br.com.alura.booking.application.service;

import br.com.alura.booking.api.sala.request.SalaRequest;
import br.com.alura.booking.api.sala.response.SalaResponse;
import br.com.alura.booking.api.sala.response.SalaResumoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SalaService {
    SalaResponse cadastraSala(SalaRequest novaSala);

    Page<SalaResumoResponse> listaSalas(Pageable pageable);

    SalaResponse buscaSalaPorId(long id);

    SalaResponse atualizaSala(long id, SalaRequest atualizaSala);

    void inativaSala(long id);

    void ativaSala(long id);
}
