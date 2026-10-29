package br.com.alura.booking.usuario.application;

import br.com.alura.booking.usuario.api.request.UsuarioRequest;
import br.com.alura.booking.usuario.api.response.UsuarioResponse;
import br.com.alura.booking.usuario.api.response.UsuarioResumoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {
    UsuarioResponse cadastraUsuario(UsuarioRequest novoUsuario);

    UsuarioResponse buscaUsuarioPorId(Long id);

    Page<UsuarioResumoResponse> listaUsuarios(Pageable pageable);

    UsuarioResponse atualizaUsuario(Long id, UsuarioRequest usuarioRequest);

    void inativaUsuario(Long id);

    void ativaUsuario(Long id);
}
