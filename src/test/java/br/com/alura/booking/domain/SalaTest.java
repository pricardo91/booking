package br.com.alura.booking.domain;

import static br.com.alura.booking.exception.ErroAssert.assertErro;
import static org.assertj.core.api.Assertions.assertThat;

import br.com.alura.booking.exception.CodigoErro;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

class SalaTest {

    private static Sala salaValida() {
        return new Sala("Sala Azul", 10);
    }

    // ---------- criação ----------

    @Test
    void criaSalaAtivaComNomeECapacidadeInformados() {
        Sala sala = new Sala("Sala Azul", 10);

        assertThat(sala.getNome()).isEqualTo("Sala Azul");
        assertThat(sala.getCapacidade()).isEqualTo(10);
        assertThat(sala.getStatus()).isEqualTo(StatusSala.ATIVA);
        assertThat(sala.isAtiva()).isTrue();
        assertThat(sala.getId()).isNull();
    }

    // ---------- capacidade ----------

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void recusaCapacidadeZeroOuNegativa(int capacidade) {
        assertErro(() -> new Sala("Sala Azul", capacidade), CodigoErro.SALA_CAPACIDADE_INVALIDA);
    }

    @Test
    void aceitaCapacidadeUm() {
        Sala sala = new Sala("Sala Azul", 1);

        assertThat(sala.getCapacidade()).isEqualTo(1);
    }

    @Test
    void setCapacidadeTambemAplicaAsRegras() {
        Sala sala = salaValida();

        assertErro(() -> sala.setCapacidade(0), CodigoErro.SALA_CAPACIDADE_INVALIDA);
        assertThat(sala.getCapacidade()).isEqualTo(10);
    }

    // ---------- atualizar ----------

    @Test
    void atualizarSalaTrocaONomeEACapacidade() {
        Sala sala = salaValida();

        sala.atualizarSala("Sala Verde", 20);

        assertThat(sala.getNome()).isEqualTo("Sala Verde");
        assertThat(sala.getCapacidade()).isEqualTo(20);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void atualizarSalaRecusaCapacidadeZeroOuNegativa(int capacidade) {
        Sala sala = salaValida();

        assertErro(() -> sala.atualizarSala("Sala Verde", capacidade), CodigoErro.SALA_CAPACIDADE_INVALIDA);
    }

    @Test
    void atualizarSalaNaoAlteraNadaQuandoACapacidadeEInvalida() {
        Sala sala = salaValida();

        assertErro(() -> sala.atualizarSala("Sala Verde", 0), CodigoErro.SALA_CAPACIDADE_INVALIDA);

        assertThat(sala.getNome()).isEqualTo("Sala Azul");
        assertThat(sala.getCapacidade()).isEqualTo(10);
    }

    @Test
    void atualizarSalaNaoMudaOStatus() {
        Sala sala = salaValida();
        sala.inativar();

        sala.atualizarSala("Sala Verde", 20);

        assertThat(sala.getStatus()).isEqualTo(StatusSala.INATIVA);
    }

    // ---------- ativar / inativar ----------

    @Test
    void inativarTornaASalaInativa() {
        Sala sala = salaValida();

        sala.inativar();

        assertThat(sala.getStatus()).isEqualTo(StatusSala.INATIVA);
        assertThat(sala.isAtiva()).isFalse();
    }

    @Test
    void ativarTornaASalaAtivaNovamente() {
        Sala sala = salaValida();
        sala.inativar();

        sala.ativar();

        assertThat(sala.getStatus()).isEqualTo(StatusSala.ATIVA);
        assertThat(sala.isAtiva()).isTrue();
    }

    @Test
    void recusaAtivarSalaQueJaEstaAtiva() {
        Sala sala = salaValida();

        assertErro(sala::ativar, CodigoErro.SALA_JA_ATIVA);
        assertThat(sala.isAtiva()).isTrue();
    }

    @Test
    void recusaInativarSalaQueJaEstaInativa() {
        Sala sala = salaValida();
        sala.inativar();

        assertErro(sala::inativar, CodigoErro.SALA_JA_INATIVA);
        assertThat(sala.isAtiva()).isFalse();
    }

    // ---------- igualdade ----------

    private static Sala salaComId(Long id) {
        Sala sala = salaValida();
        ReflectionTestUtils.setField(sala, "id", id);
        return sala;
    }

    @Test
    void salasComOMesmoIdSaoIguais() {
        Sala a = salaComId(1L);
        Sala b = salaComId(1L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void salasComIdsDiferentesNaoSaoIguais() {
        assertThat(salaComId(1L)).isNotEqualTo(salaComId(2L));
    }

    @Test
    void salasSemIdSoSaoIguaisASiMesmas() {
        Sala a = salaValida();
        Sala b = salaValida();

        assertThat(a).isEqualTo(a);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void salaNaoEIgualANuloNemAOutroTipo() {
        Sala sala = salaComId(1L);

        assertThat(sala.equals(null)).isFalse();
        assertThat(sala.equals("Sala Azul")).isFalse();
    }
}
