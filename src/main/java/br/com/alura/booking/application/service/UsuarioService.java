package br.com.alura.booking.application.service;

import br.com.alura.booking.api.usuario.request.UsuarioRequest;
import br.com.alura.booking.api.usuario.response.UsuarioResponse;

public interface UsuarioService {
    UsuarioResponse cadastraUsuario(UsuarioRequest novoUsuario);

    UsuarioResponse buscaUsuarioPorId(Long id);
}
