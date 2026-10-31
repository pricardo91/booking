package br.com.alura.booking.reserva.api;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservas", description = "Endpoints para gerenciamento de reservas")
@RequestMapping("/api/v1/reservas")
public interface ReservaAPI {

    @Operation(
            summary = "Cadastrar reserva",
            description = "Cria uma reserva PENDENTE para um usuário ativo em uma sala ativa")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Reserva criada com sucesso",
                        content = @Content(schema = @Schema(implementation = ReservaResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Request Body inválido ou inexistente!",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Usuário ou sala não encontrado",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "422",
                        description = "Usuário ou sala inativo, ou intervalo inválido",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ReservaResponse cadastraReserva(@Valid @RequestBody ReservaRequest novaReserva);
}
