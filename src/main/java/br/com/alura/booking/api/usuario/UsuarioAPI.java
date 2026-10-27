package br.com.alura.booking.api.usuario;

import br.com.alura.booking.api.usuario.request.UsuarioRequest;
import br.com.alura.booking.api.usuario.response.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/usuarios")
public interface UsuarioAPI {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UsuarioResponse cadastraUsuario(@Valid @RequestBody UsuarioRequest novoUsuario);
}
