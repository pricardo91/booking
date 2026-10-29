package br.com.alura.booking.sala.application;

import br.com.alura.booking.sala.api.request.SalaRequest;
import br.com.alura.booking.sala.api.response.SalaResponse;
import br.com.alura.booking.sala.api.response.SalaResumoResponse;
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
