package br.com.alura.booking.api.sala.response;

import br.com.alura.booking.domain.Sala;
import br.com.alura.booking.domain.StatusSala;
import lombok.Getter;

@Getter
public class SalaResponse {

    private final Long id;
    private final String nome;
    private final Integer capacidade;
    private final StatusSala statusSala;

    public SalaResponse(Sala sala) {
        this.id = sala.getId();
        this.nome = sala.getNome();
        this.capacidade = sala.getCapacidade();
        this.statusSala = sala.getStatus();
    }
}
