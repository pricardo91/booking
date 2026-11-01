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
    "id_usuario",
    "id_sala",
    "usuarioNome",
    "salaNome",
    "status",
    "inicio",
    "fim",
    "cadastradoEm",
    "atualizadoEm"
})
public class ReservaResumoResponse {

    private final Long id;
    private final Long id_usuario;
    private final Long id_sala;
    private final String usuarioNome;
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

    public ReservaResumoResponse(Reserva reserva) {

        this.id = reserva.getId();
        this.id_sala = reserva.getSala().getId();
        this.id_usuario = reserva.getUsuario().getId();
        this.usuarioNome = reserva.getUsuario().getNome();
        this.salaNome = reserva.getSala().getNome();
        this.status = reserva.getStatus();
        this.inicio = reserva.getInicio();
        this.fim = reserva.getFim();
        this.cadastradoEm = reserva.getCadastradoEm();
        this.atualizadoEm = reserva.getAtualizadoEm();
    }
}
