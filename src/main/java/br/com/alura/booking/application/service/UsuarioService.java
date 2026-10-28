package br.com.alura.booking.application.service;

import br.com.alura.booking.api.usuario.request.UsuarioRequest;
import br.com.alura.booking.api.usuario.response.UsuarioResponse;
import br.com.alura.booking.api.usuario.response.UsuarioResumoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {
    UsuarioResponse cadastraUsuario(UsuarioRequest novoUsuario);

    UsuarioResponse buscaUsuarioPorId(Long id);

    Page<UsuarioResumoResponse> listaUsuarios(Pageable pageable);

    UsuarioResponse atualizaUsuario(Long id, UsuarioRequest usuarioRequest);
}
