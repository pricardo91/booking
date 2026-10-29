package br.com.alura.booking.sala.api.response;

import br.com.alura.booking.sala.domain.Sala;
import br.com.alura.booking.sala.domain.StatusSala;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"id", "nome", "capacidade", "status"})
public class SalaResumoResponse {

    private final Long id;
    private final String nome;
    private final Integer capacidade;
    private final StatusSala status;

    public SalaResumoResponse(Sala sala) {
        this.id = sala.getId();
        this.nome = sala.getNome();
        this.capacidade = sala.getCapacidade();
        this.status = sala.getStatus();
    }
}
