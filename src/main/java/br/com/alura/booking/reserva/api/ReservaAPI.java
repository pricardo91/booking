package br.com.alura.booking.reserva.api;

import br.com.alura.booking.reserva.api.request.ReservaRequest;
import br.com.alura.booking.reserva.api.response.ReservaHistoricoResponse;
import br.com.alura.booking.reserva.api.response.ReservaResponse;
import br.com.alura.booking.reserva.api.response.ReservaResumoResponse;
import br.com.alura.booking.reserva.domain.StatusReserva;
import br.com.alura.booking.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
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

    @Operation(
            summary = "Confirmar reserva",
            description = "Confirma uma reserva PENDENTE, que passa a ocupar a sala. As outras reservas PENDENTE da"
                    + " mesma sala que se sobrepõem a ela são canceladas automaticamente (vale o primeiro a ser"
                    + " confirmado). A mudança de cada reserva fica registrada no histórico")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Reserva confirmada com sucesso",
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
                        responseCode = "409",
                        description = "A reserva não está PENDENTE, ou já existe uma reserva ativa na sala neste"
                                + " horário",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PatchMapping("/{idReserva}/confirma")
    ReservaResponse confirmaReserva(@PathVariable Long idReserva);

    @Operation(
            summary = "Cancelar reserva",
            description = "Cancela uma reserva PENDENTE ou ATIVO e libera o horário na sala. "
                    + "A mudança de status fica registrada no histórico")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Reserva cancelada com sucesso",
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
                        responseCode = "409",
                        description = "A reserva já está CANCELADA ou já foi FINALIZADA",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PatchMapping("/{idReserva}/cancela")
    ReservaResponse cancelaReserva(@PathVariable Long idReserva);

    @Operation(
            summary = "Listar histórico da reserva",
            description = "Lista as mudanças de status de uma reserva, da mais antiga para a mais recente. "
                    + "Cada registro traz o status anterior, o novo, o motivo e o momento da mudança")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Histórico da reserva (lista vazia se ela ainda não mudou de status)",
                        content =
                                @Content(
                                        array =
                                                @ArraySchema(
                                                        schema =
                                                                @Schema(
                                                                        implementation =
                                                                                ReservaHistoricoResponse.class)))),
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
    @GetMapping("/{idReserva}/historico")
    List<ReservaHistoricoResponse> historicoReserva(@PathVariable Long idReserva);
}
