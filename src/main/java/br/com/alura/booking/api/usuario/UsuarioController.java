package br.com.alura.booking.api.usuario;

import br.com.alura.booking.api.common.PageResponse;
import br.com.alura.booking.api.usuario.request.UsuarioRequest;
import br.com.alura.booking.api.usuario.response.UsuarioResponse;
import br.com.alura.booking.api.usuario.response.UsuarioResumoResponse;
import br.com.alura.booking.application.service.UsuarioService;
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
}
