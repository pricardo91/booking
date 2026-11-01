package br.com.alura.booking.sala.api;

import br.com.alura.booking.sala.api.request.SalaRequest;
import br.com.alura.booking.sala.api.response.SalaResponse;
import br.com.alura.booking.sala.api.response.SalaResumoResponse;
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

@Tag(name = "Salas", description = "Endpoints para gerenciamento de salas")
@RequestMapping("/api/v1/salas")
public interface SalaAPI {

    @Operation(summary = "Cadastrar sala", description = "Cria uma nova sala no sistema")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Sala criada com sucesso",
                        content = @Content(schema = @Schema(implementation = SalaResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Request Body inválido ou inexistente!",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "Já existe uma sala com este nome",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SalaResponse cadastraSala(@Valid @RequestBody SalaRequest novaSala);

    @Operation(summary = "Listar salas", description = "Lista as salas cadastradas de forma paginada")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Salas listadas com sucesso",
                        content = @Content(schema = @Schema(implementation = PageResponse.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @GetMapping
    PageResponse<SalaResumoResponse> listaSalas(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(100) int size);

    @Operation(summary = "Buscar sala por id", description = "Busca uma sala cadastrada pelo id")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Sala encontrada com sucesso",
                        content = @Content(schema = @Schema(implementation = SalaResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Sala não encontrada",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @GetMapping("/{id}")
    SalaResponse buscaSalaPorId(@PathVariable long id);

    @Operation(summary = "Atualizar sala", description = "Atualiza o nome e a capacidade de uma sala pelo id")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Sala atualizada com sucesso",
                        content = @Content(schema = @Schema(implementation = SalaResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Request Body inválido ou inexistente!",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Sala não encontrada",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "Já existe outra sala com este nome",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    SalaResponse atualizaSala(@PathVariable long id, @Valid @RequestBody SalaRequest atualizaSala);

    @Operation(summary = "Inativar sala", description = "Inativa uma sala pelo id")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Sala inativada com sucesso",
                        content = @Content(schema = @Schema())),
                @ApiResponse(
                        responseCode = "404",
                        description = "Sala não encontrada",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "Sala já está inativa",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PatchMapping("/{id}/inativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void inativaSala(@PathVariable long id);

    @Operation(summary = "Ativar sala", description = "Ativa uma sala pelo id")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Sala ativada com sucesso",
                        content = @Content(schema = @Schema())),
                @ApiResponse(
                        responseCode = "404",
                        description = "Sala não encontrada",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "Sala já está ativa",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PatchMapping("/{id}/ativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void ativaSala(@PathVariable long id);
}
