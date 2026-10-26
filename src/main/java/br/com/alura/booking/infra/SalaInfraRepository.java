package br.com.alura.booking.infra;

import br.com.alura.booking.application.repository.SalaRepository;
import br.com.alura.booking.domain.Sala;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        return salaJPARepository.existsByNomeIgnoreCase(nome);
    }
}
