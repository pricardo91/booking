package br.com.alura.booking.api.usuario;

import br.com.alura.booking.api.common.PageResponse;
import br.com.alura.booking.api.usuario.request.UsuarioRequest;
import br.com.alura.booking.api.usuario.response.UsuarioResponse;
import br.com.alura.booking.api.usuario.response.UsuarioResumoResponse;
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

@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
@RequestMapping("/api/v1/usuarios")
public interface UsuarioAPI {

    @Operation(summary = "Cadastrar usuário", description = "Cria um novo usuário no sistema")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Usuário criado com sucesso",
                        content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Request Body inválido ou inexistente!",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "Já existe um usuário com este CPF ou e-mail",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UsuarioResponse cadastraUsuario(@Valid @RequestBody UsuarioRequest novoUsuario);

    @Operation(summary = "Buscar usuário por id", description = "Buscar um usuário cadastrado no sistema")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Usuário encontrado com sucesso",
                        content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Usuário não encontrado",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @GetMapping("/{id}")
    UsuarioResponse buscaUsuarioPorId(@PathVariable Long id);

    @Operation(
            summary = "Listar usuários",
            description = "Lista os usuários cadastrados de forma paginada, ordenados pelo nome")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Usuários listados com sucesso",
                        content = @Content(schema = @Schema(implementation = PageResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Parâmetros de paginação inválidos",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @GetMapping
    PageResponse<UsuarioResumoResponse> listaUsuario(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(100) int size);

    @Operation(summary = "Atualizar usuário", description = "Atualiza nome, e-mail e CPF de um usuário cadastrado")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Usuário atualizado com sucesso",
                        content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Request Body inválido ou inexistente!",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Usuário não encontrado",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "Já existe outro usuário com este CPF ou e-mail",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno no servidor",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PatchMapping("/{id}")
    UsuarioResponse atualizaUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequest usuarioRequest);
}
