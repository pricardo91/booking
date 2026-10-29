package br.com.alura.booking.sala.api;

import br.com.alura.booking.sala.api.request.SalaRequest;
import br.com.alura.booking.sala.api.response.SalaResponse;
import br.com.alura.booking.sala.api.response.SalaResumoResponse;
import br.com.alura.booking.sala.application.SalaService;
import br.com.alura.booking.shared.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SalaController implements SalaAPI {

    private final SalaService salaService;

    @Override
    public SalaResponse cadastraSala(SalaRequest novaSala) {
        log.debug("[start] - {} - cadastraSala", getClass().getSimpleName());
        SalaResponse salaNova = salaService.cadastraSala(novaSala);
        log.debug("[finish] - {} - cadastraSala", getClass().getSimpleName());
        return salaNova;
    }

    @Override
    public PageResponse<SalaResumoResponse> listaSala(int page, int size) {
        log.debug("[start] - {} - listaSala", getClass().getSimpleName());
        Page<SalaResumoResponse> salaPaginada = salaService.listaSalas(PageRequest.of(page, size, Sort.by("nome")));
        log.debug("[finish] - {} - listaSala", getClass().getSimpleName());
        return PageResponse.from(salaPaginada);
    }

    @Override
    public SalaResponse buscaSalaPorId(long id) {
        log.debug("[start] - {} - buscaSalaPorId", getClass().getSimpleName());
        SalaResponse response = salaService.buscaSalaPorId(id);
        return response;
    }

    @Override
    public SalaResponse atualizaSala(long id, @Valid SalaRequest atualizaSala) {
        log.debug("[start] - {} - atualizaSala", getClass().getSimpleName());
        SalaResponse salaResponse = salaService.atualizaSala(id, atualizaSala);
        log.debug("[finish] - {} - atualizaSala", getClass().getSimpleName());
        return salaResponse;
    }

    @Override
    public void inativaSala(long id) {
        log.debug("[start] - {} - inativaSala", getClass().getSimpleName());
        salaService.inativaSala(id);
        log.debug("[finish] - {} - inativaSala", getClass().getSimpleName());
    }

    @Override
    public void ativaSala(long id) {
        log.debug("[start] - {} - ativaSala", getClass().getSimpleName());
        salaService.ativaSala(id);
        log.debug("[finish] - {} - ativaSala", getClass().getSimpleName());
    }
}
