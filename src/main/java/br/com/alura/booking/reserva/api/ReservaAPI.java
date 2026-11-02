package br.com.alura.booking.reserva.api;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaResponse;
import br.com.alura.booking.reserva.api.response.ReservaResumoResponse;
import br.com.alura.booking.reserva.domain.StatusReserva;
import br.com.alura.booking.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Operation(summary = "Buscar reserva por id", description = "Busca uma reserva pelo id informado")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Reserva encontrada com sucesso",
                        content = @Content(schema = @Schema(implementation = ReservaResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Id com formato inválido",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Reserva não encontrada",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @GetMapping("/{id}")
    ReservaResponse buscaReservaPorId(@PathVariable Long id);

    @Operation(
            summary = "Listar reservas",
            description = "Lista as reservas de forma paginada, da mais recente para a mais antiga."
                    + " Os filtros status, idSala e idUsuario são opcionais e podem ser combinados")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Reservas listadas com sucesso",
                        content = @Content(schema = @Schema(implementation = PageResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Parâmetro inválido (page, size, status, idSala ou idUsuario)",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @GetMapping
    PageResponse<ReservaResumoResponse> listaReservas(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(100) int size,
            @RequestParam(required = false) StatusReserva status,
            @RequestParam(required = false) Long idSala,
            @RequestParam(required = false) Long idUsuario);
}
