package br.com.alura.booking.reserva.api.response;

import br.com.alura.booking.reserva.domain.MotivoHistorico;
import br.com.alura.booking.reserva.domain.ReservaHistorico;
import br.com.alura.booking.reserva.domain.StatusReserva;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"statusAnterior", "statusNovo", "motivo", "criadoEm"})
public class ReservaHistoricoResponse {
    private final StatusReserva statusAnterior;
    private final StatusReserva statusNovo;
    private final MotivoHistorico motivo;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private final LocalDateTime criadoEm;

    public ReservaHistoricoResponse(ReservaHistorico reservaHistorico) {
        this.statusAnterior = reservaHistorico.getStatusAnterior();
        this.statusNovo = reservaHistorico.getStatusNovo();
        this.motivo = reservaHistorico.getMotivo();
        this.criadoEm = reservaHistorico.getCriadoEm();
    }
}
