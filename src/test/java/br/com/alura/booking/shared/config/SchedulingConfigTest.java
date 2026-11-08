package br.com.alura.booking.shared.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.alura.booking.PostgresTesteConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testa o cadeado do ShedLock contra um Postgres real (Testcontainers), com a tabela criada pela
 * migration V3. Cada {@link LockProvider} representa uma instância da aplicação; as duas usam o
 * mesmo banco, como duas instâncias de verdade.
 *
 * <p>Sem transação de teste: o ShedLock grava o cadeado na própria transação, e o cadeado só vale
 * para a outra instância depois de gravado.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTesteConfig.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SchedulingConfigTest {

    @Autowired
    private DataSource dataSource;

    private LockProvider instanciaA;
    private LockProvider instanciaB;

    @BeforeEach
    void criaDuasInstancias() {
        instanciaA = new SchedulingConfig().lockProvider(dataSource);
        instanciaB = new SchedulingConfig().lockProvider(dataSource);
    }

    @Test
    void soUmaInstanciaPegaOCadeadoPorVez() {
        LockConfiguration cadeado = cadeado("job-concorrente");

        Optional<SimpleLock> daA = instanciaA.lock(cadeado);
        Optional<SimpleLock> daB = instanciaB.lock(cadeado);

        assertThat(daA).isPresent();
        assertThat(daB).isEmpty();
        daA.get().unlock();
    }

    @Test
    void outraInstanciaPegaOCadeadoDepoisQueAPrimeiraSolta() {
        LockConfiguration cadeado = cadeado("job-em-sequencia");
        SimpleLock daA = instanciaA.lock(cadeado).orElseThrow();
        daA.unlock();

        Optional<SimpleLock> daB = instanciaB.lock(cadeado);

        assertThat(daB).isPresent();
        daB.get().unlock();
    }

    @Test
    void cadeadosComNomesDiferentesNaoSeBloqueiam() {
        Optional<SimpleLock> primeiro = instanciaA.lock(cadeado("job-um"));
        Optional<SimpleLock> segundo = instanciaB.lock(cadeado("job-dois"));

        assertThat(primeiro).isPresent();
        assertThat(segundo).isPresent();
        primeiro.get().unlock();
        segundo.get().unlock();
    }

    /** Cadeado de até 1 minuto, sem tempo mínimo: ao soltar, já fica livre. */
    private static LockConfiguration cadeado(String nome) {
        return new LockConfiguration(Instant.now(), nome, Duration.ofMinutes(1), Duration.ZERO);
    }
}
