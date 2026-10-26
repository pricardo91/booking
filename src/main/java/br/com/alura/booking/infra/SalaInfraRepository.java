package br.com.alura.booking.infra;

import br.com.alura.booking.application.repository.SalaRepository;
import br.com.alura.booking.domain.Sala;
import br.com.alura.booking.exception.CodigoErro;
import br.com.alura.booking.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SalaInfraRepository implements SalaRepository {

    private final SalaJPARepository salaJPARepository;

    @Override
    public Sala salva(Sala sala) {
        log.debug("[start] - {} - salva", getClass().getSimpleName());
        var salaSalva = salaJPARepository.save(sala);
        log.debug("[finish] - {} - salva", getClass().getSimpleName());
        return salaSalva;
    }

    @Override
    public boolean existeSalaPorNome(String nome) {
        log.debug("[start] - {} - existeSalaPorNome", getClass().getSimpleName());
        return salaJPARepository.existsByNomeIgnoreCase(nome);
    }

    @Override
    public Sala buscaSalaPorId(long id) {
        log.debug("[start] - {} - buscaSalaPorId", getClass().getSimpleName());
        Sala sala = salaJPARepository
                .findById(id)
                .orElseThrow(() -> new NegocioException(CodigoErro.SALA_NAO_ENCONTRADA, String.valueOf(id)));
        log.debug("[finish] - {} - buscaSalaPorId", getClass().getSimpleName());
        return sala;
    }

    @Override
    public Page<Sala> listaSalas(Pageable pageable) {
        log.debug("[start] - {} - listaSalas", getClass().getSimpleName());
        Page<Sala> salas = salaJPARepository.findAll(pageable);
        log.debug("[finish] - {} - listaSalas", getClass().getSimpleName());
        return salas;
    }
}
