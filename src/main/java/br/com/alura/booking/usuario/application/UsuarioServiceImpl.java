package br.com.alura.booking.usuario.application;

import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import br.com.alura.booking.usuario.api.request.UsuarioRequest;
import br.com.alura.booking.usuario.api.response.UsuarioResponse;
import br.com.alura.booking.usuario.api.response.UsuarioResumoResponse;
import br.com.alura.booking.usuario.domain.Usuario;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UsuarioResponse cadastraUsuario(UsuarioRequest novoUsuario) {
        log.debug("[start] - {} - cadastraUsuario", getClass().getSimpleName());
        var nomeLimpo = normalizaNome(novoUsuario.getNome());
        var emailLimpo = normalizaEmail(novoUsuario.getEmail());
        var cpfSoDigitos = normalizaCpf(novoUsuario.getCpf());

        if (usuarioRepository.existeUsuarioPorEmail(emailLimpo)) {
            throw new NegocioException(CodigoErro.USUARIO_EMAIL_DUPLICADO, emailLimpo);
        }
        if (usuarioRepository.existeUsuarioPorCpf(cpfSoDigitos)) {
            throw new NegocioException(CodigoErro.USUARIO_CPF_DUPLICADO);
        }

        Usuario usuarioCriado = usuarioRepository.salva(new Usuario(nomeLimpo, emailLimpo, cpfSoDigitos));
        UsuarioResponse usuarioResponse = new UsuarioResponse(usuarioCriado);
        log.debug("[finish] - {} - cadastraUsuario", getClass().getSimpleName());
        return usuarioResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscaUsuarioPorId(Long id) {
        log.debug("[start] - {} - buscaUsuarioPorId", getClass().getSimpleName());
        Usuario usuario = usuarioRepository.buscaUsuarioPorId(id);
        UsuarioResponse usuarioResponse = new UsuarioResponse(usuario);
        log.debug("[finish] - {} - buscaUsuarioPorId", getClass().getSimpleName());
        return usuarioResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResumoResponse> listaUsuarios(Pageable pageable) {
        log.debug("[start] - {} - listaUsuarios", getClass().getSimpleName());
        Page<Usuario> usuariosPage = usuarioRepository.listaUsuarios(pageable);
        log.debug("[finish] - {} - listaUsuarios", getClass().getSimpleName());
        return usuariosPage.map(UsuarioResumoResponse::new);
    }

    @Override
    @Transactional
    public UsuarioResponse atualizaUsuario(Long id, UsuarioRequest usuarioRequest) {
        log.debug("[start] - {} - atualizaUsuario", getClass().getSimpleName());

        var nomeLimpo = normalizaNome(usuarioRequest.getNome());
        var emailLimpo = normalizaEmail(usuarioRequest.getEmail());
        var cpfSoDigitos = normalizaCpf(usuarioRequest.getCpf());

        Usuario usuario = usuarioRepository.buscaUsuarioPorId(id);

        if (usuarioRepository.existeOutroUsuarioPorEmail(emailLimpo, id)) {
            throw new NegocioException(CodigoErro.USUARIO_EMAIL_DUPLICADO, emailLimpo);
        }
        if (usuarioRepository.existeOutroUsuarioPorCpf(cpfSoDigitos, id)) {
            throw new NegocioException(CodigoErro.USUARIO_CPF_DUPLICADO);
        }

        usuario.atualizarUsuario(nomeLimpo, emailLimpo, cpfSoDigitos);

        usuarioRepository.salva(usuario);

        UsuarioResponse usuarioResponse = new UsuarioResponse(usuario);

        log.debug("[finish] - {} - atualizaUsuario", getClass().getSimpleName());
        return usuarioResponse;
    }

    @Override
    @Transactional
    public void inativaUsuario(Long id) {
        log.debug("[start] - {} - inativaUsuario", getClass().getSimpleName());
        Usuario usuario = usuarioRepository.buscaUsuarioPorId(id);
        usuario.inativar();
        log.debug("[finish] - {} - inativaUsuario", getClass().getSimpleName());
    }

    @Override
    @Transactional
    public void ativaUsuario(Long id) {
        log.debug("[start] - {} - ativaUsuario", getClass().getSimpleName());
        Usuario usuario = usuarioRepository.buscaUsuarioPorId(id);
        usuario.ativar();
        log.debug("[finish] - {} - ativaUsuario", getClass().getSimpleName());
    }

    private String normalizaNome(String nome) {
        return nome.strip();
    }

    private String normalizaEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private String normalizaCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }
}
