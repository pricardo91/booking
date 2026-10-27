package br.com.alura.booking.api.usuario;

import br.com.alura.booking.api.usuario.request.UsuarioRequest;
import br.com.alura.booking.api.usuario.response.UsuarioResponse;
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
}
