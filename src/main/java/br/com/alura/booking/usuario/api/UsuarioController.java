package br.com.alura.booking.usuario.api;

import br.com.alura.booking.shared.api.PageResponse;
import br.com.alura.booking.usuario.api.request.UsuarioRequest;
import br.com.alura.booking.usuario.api.response.UsuarioResponse;
import br.com.alura.booking.usuario.api.response.UsuarioResumoResponse;
import br.com.alura.booking.usuario.application.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UsuarioController implements UsuarioAPI {

    private final UsuarioService usuarioService;

    @Override
    public UsuarioResponse cadastraUsuario(UsuarioRequest novoUsuario) {
        log.debug("[start] - {} - cadastraUsuario", getClass().getSimpleName());
        UsuarioResponse usuarioNovo = usuarioService.cadastraUsuario(novoUsuario);
        log.debug("[finish] - {} - cadastraUsuario", getClass().getSimpleName());
        return usuarioNovo;
    }

    @Override
    public UsuarioResponse buscaUsuarioPorId(Long id) {
        log.debug("[start] - {} - buscaUsuarioPorId", getClass().getSimpleName());
        UsuarioResponse usuario = usuarioService.buscaUsuarioPorId(id);
        log.debug("[finish] - {} - buscaUsuarioPorId", getClass().getSimpleName());
        return usuario;
    }

    @Override
    public PageResponse<UsuarioResumoResponse> listaUsuario(int page, int size) {
        log.debug("[start] - {} - listaUsuario", getClass().getSimpleName());
        Page<UsuarioResumoResponse> usuarioResumoResponses =
                usuarioService.listaUsuarios(PageRequest.of(page, size, Sort.by("nome")));
        log.debug("[finish] - {} - listaUsuario", getClass().getSimpleName());
        return PageResponse.from(usuarioResumoResponses);
    }

    @Override
    public UsuarioResponse atualizaUsuario(Long id, UsuarioRequest usuarioRequest) {
        log.debug("[start] - {} - atualizaUsuario", getClass().getSimpleName());
        UsuarioResponse usuarioAtualizado = usuarioService.atualizaUsuario(id, usuarioRequest);
        log.debug("[finish] - {} - atualizaUsuario", getClass().getSimpleName());
        return usuarioAtualizado;
    }

    @Override
    public void inativaUsuario(Long id) {
        log.debug("[start] - {} - inativaUsuario", getClass().getSimpleName());
        usuarioService.inativaUsuario(id);
        log.debug("[finish] - {} - inativaUsuario", getClass().getSimpleName());
    }

    @Override
    public void ativaUsuario(Long id) {
        log.debug("[start] - {} - ativaUsuario", getClass().getSimpleName());
        usuarioService.ativaUsuario(id);
        log.debug("[finish] - {} - ativaUsuario", getClass().getSimpleName());
    }
}
