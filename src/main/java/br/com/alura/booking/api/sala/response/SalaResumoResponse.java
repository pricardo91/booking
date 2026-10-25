package br.com.alura.booking.api.sala.response;

import br.com.alura.booking.domain.Sala;
import br.com.alura.booking.domain.StatusSala;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SalaResumoResponse {

    private Long id;
    private String nome;
    private Integer capacidade;
    private StatusSala statusSala;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataCriacao;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataEdicao;

    public SalaResumoResponse(Sala sala) {
        this.id = sala.getId();
        this.nome = sala.getNome();
        this.capacidade = sala.getCapacidade();
        this.statusSala = sala.getStatus();
        this.dataCriacao = sala.getCadastradoEm();
        this.dataEdicao = sala.getAtualizadoEm();
    }
}
