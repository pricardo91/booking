package br.com.alura.booking.application.service;

import br.com.alura.booking.api.sala.request.SalaRequest;
import br.com.alura.booking.api.sala.response.SalaResponse;
import br.com.alura.booking.application.repository.SalaRepository;
import br.com.alura.booking.domain.Sala;
import br.com.alura.booking.exception.CodigoErro;
import br.com.alura.booking.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class SalaServiceImpl implements SalaService {

    private final SalaRepository salaRepository;

    @Override
    @Transactional
    public SalaResponse cadastraSala(SalaRequest novaSala) {
        log.debug("[start] - {} - cadastraSala", getClass().getSimpleName());
        var nomeLimpo = novaSala.getNome().strip();

        if (salaRepository.existeSalaPorNome(nomeLimpo)) {
            throw new NegocioException(CodigoErro.SALA_NOME_DUPLICADO, nomeLimpo);
        }

        Sala salaCriada = salaRepository.salva(new Sala(nomeLimpo, novaSala.getCapacidade()));
        SalaResponse salaResponse = new SalaResponse(salaCriada);
        log.debug("[finish] - {} - cadastraSala", getClass().getSimpleName());

        return salaResponse;
    }

    @Override
    public Page<Sala> listaSalas(Pageable pageable) {
        log.debug("[start] - {} - listaSalas", getClass().getSimpleName());
        return Page.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public SalaResponse buscaSalaPorId(long id) {
        log.debug("[start] - {} - buscaSalaPorId", getClass().getSimpleName());
        Sala sala = salaRepository.buscaSalaPorId(id);
        return new SalaResponse(sala);
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
