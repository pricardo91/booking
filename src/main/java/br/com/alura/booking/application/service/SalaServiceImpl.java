package br.com.alura.booking.application.service;

import br.com.alura.booking.api.sala.request.SalaRequest;
import br.com.alura.booking.api.sala.response.SalaResponse;
import br.com.alura.booking.domain.Sala;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class SalaServiceImpl implements SalaService {
    @Override
    public SalaResponse cadastraSala(SalaRequest novaSala) {
        log.debug("[start] - {} - cadastraSala", getClass().getSimpleName());
        log.debug("[finish] - {} - cadastraSala", getClass().getSimpleName());

        return null;
    }

    @Override
    public Page<Sala> listaSalas(Pageable pageable) {
        log.debug("[start] - {} - listaSalas", getClass().getSimpleName());
        return Page.empty();
    }

    @Override
    public SalaResponse buscaSala(long id) {
        log.debug("[start] - {} - buscaSala", getClass().getSimpleName());
        return null;
    }

    @Override
    public void atualizaSala(long id) {
        log.debug("[start] - {} - atualizaSala", getClass().getSimpleName());
        log.debug("[finish] - {} - atualizaSala", getClass().getSimpleName());
    }

    @Override
    public void inativaSala(long id) {
        log.debug("[start] - {} - inativaSala", getClass().getSimpleName());
        log.debug("[finish] - {} - inativaSala", getClass().getSimpleName());
    }

    @Override
    public void ativaSala(long id) {
        log.debug("[start] - {} - ativaSala", getClass().getSimpleName());
        log.debug("[finish] - {} - ativaSala", getClass().getSimpleName());
    }
}
