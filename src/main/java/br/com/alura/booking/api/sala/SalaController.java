package br.com.alura.booking.api.sala;

import br.com.alura.booking.api.sala.request.SalaRequest;
import br.com.alura.booking.api.sala.response.PageResponse;
import br.com.alura.booking.api.sala.response.SalaResponse;
import br.com.alura.booking.api.sala.response.SalaResumoResponse;
import br.com.alura.booking.domain.Sala;
import br.com.alura.booking.application.service.SalaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SalaController implements SalaAPI {

    private final SalaService salaService;

    @Override
    public SalaResponse cadastraSala(SalaRequest novaSala) {
        log.debug("[start] - " + getClass().getSimpleName() + " - cadastraSala");
        SalaResponse salaNova = salaService.cadastraSala(novaSala);
        log.debug("[finish] - " + getClass().getSimpleName() + " - cadastraSala");
        return salaNova;
    }

    @Override
    public PageResponse<SalaResumoResponse> listaSala(int page, int size) {
        log.debug("[start] - " + getClass().getSimpleName() + " - listaSala");
        Page<Sala> salas = salaService.listaSalas(PageRequest.of(page, size));
        Page<SalaResumoResponse> response = salas.map(SalaResumoResponse::new);
        log.debug("[finish] - " + getClass().getSimpleName() + " - listaSala");
        return PageResponse.from(response);
    }

    @Override
    public SalaResponse buscaSala(long id) {
        log.debug("[start] - " + getClass().getSimpleName() + " - buscaSala");
        SalaResponse response = salaService.buscaSala(id);
        return response;
    }

    @Override
    public void atualizaSala(long id, @Valid SalaRequest atualizaSala) {
        log.debug("[start] - " + getClass().getSimpleName() + " - atualizaSala");
        salaService.atualizaSala(id);
        log.debug("[finish] - " + getClass().getSimpleName() + " - atualizaSala");
    }

    @Override
    public void inativaSala(long id) {
        log.debug("[start] - " + getClass().getSimpleName() + " - inativaSala");
        salaService.inativaSala(id);
        log.debug("[finish] - " + getClass().getSimpleName() + " - inativaSala");
    }

    @Override
    public void ativaSala(long id) {
        log.debug("[start] - " + getClass().getSimpleName() + " - ativaSala");
        salaService.ativaSala(id);
        log.debug("[finish] - " + getClass().getSimpleName() + " - ativaSala");
    }
}
