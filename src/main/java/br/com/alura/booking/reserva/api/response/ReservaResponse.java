package br.com.alura.booking.reserva.api.response;

import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.StatusReserva;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
    "id",
    "idUsuario",
    "usuarioNome",
    "salaId",
    "salaNome",
    "status",
    "inicio",
    "fim",
    "cadastradoEm",
    "atualizadoEm"
})
public class ReservaResponse {

    private final Long id;
    private final Long usuarioId;
    private final String usuarioNome;
    private final Long salaId;
    private final String salaNome;
    private final StatusReserva status;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private final LocalDateTime inicio;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private final LocalDateTime fim;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private final LocalDateTime cadastradoEm;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private final LocalDateTime atualizadoEm;

    /** Usuário e sala são LAZY: monte a resposta dentro da transação do service. */
    public ReservaResponse(Reserva reserva) {
        this.id = reserva.getId();
        this.usuarioId = reserva.getUsuario().getId();
        this.usuarioNome = reserva.getUsuario().getNome();
        this.salaId = reserva.getSala().getId();
        this.salaNome = reserva.getSala().getNome();
        this.status = reserva.getStatus();
        this.inicio = reserva.getInicio();
        this.fim = reserva.getFim();
        this.cadastradoEm = reserva.getCadastradoEm();
        this.atualizadoEm = reserva.getAtualizadoEm();
    }
}
