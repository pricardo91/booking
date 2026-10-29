package br.com.alura.booking.sala.application;

import br.com.alura.booking.sala.api.request.SalaRequest;
import br.com.alura.booking.sala.api.response.SalaResponse;
import br.com.alura.booking.sala.api.response.SalaResumoResponse;
import br.com.alura.booking.sala.domain.Sala;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
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
    @Transactional(readOnly = true)
    public Page<SalaResumoResponse> listaSalas(Pageable pageable) {
        log.debug("[start] - {} - listaSalas", getClass().getSimpleName());
        Page<Sala> salas = salaRepository.listaSalas(pageable);
        return salas.map(SalaResumoResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public SalaResponse buscaSalaPorId(long id) {
        log.debug("[start] - {} - buscaSalaPorId", getClass().getSimpleName());
        Sala sala = salaRepository.buscaSalaPorId(id);
        return new SalaResponse(sala);
    }

    @Override
    @Transactional
    public SalaResponse atualizaSala(long id, SalaRequest atualizaSala) {
        log.debug("[start] - {} - atualizaSala", getClass().getSimpleName());

        var nomeLimpo = atualizaSala.getNome().strip();
        Sala sala = salaRepository.buscaSalaPorId(id);

        if (salaRepository.existeOutraSalaPorNome(nomeLimpo, id)) {
            throw new NegocioException(CodigoErro.SALA_NOME_DUPLICADO, nomeLimpo);
        }

        sala.atualizarSala(nomeLimpo, atualizaSala.getCapacidade());

        sala = salaRepository.salva(sala);
        log.debug("[finish] - {} - atualizaSala", getClass().getSimpleName());
        return new SalaResponse(sala);
    }

    @Override
    @Transactional
    public void inativaSala(long id) {
        log.debug("[start] - {} - inativaSala", getClass().getSimpleName());
        Sala sala = salaRepository.buscaSalaPorId(id);
        sala.inativar();
        log.debug("[finish] - {} - inativaSala", getClass().getSimpleName());
    }

    @Override
    @Transactional
    public void ativaSala(long id) {
        log.debug("[start] - {} - ativaSala", getClass().getSimpleName());
        var sala = salaRepository.buscaSalaPorId(id);
        sala.ativar();
        log.debug("[finish] - {} - ativaSala", getClass().getSimpleName());
    }
}
