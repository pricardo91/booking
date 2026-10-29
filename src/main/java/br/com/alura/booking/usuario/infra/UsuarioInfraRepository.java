package br.com.alura.booking.usuario.infra;

import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import br.com.alura.booking.usuario.application.UsuarioRepository;
import br.com.alura.booking.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UsuarioInfraRepository implements UsuarioRepository {

    private final UsuarioJPARepository usuarioJPARepository;

    @Override
    public Usuario salva(Usuario usuario) {
        log.debug("[start] - {} - salva", getClass().getSimpleName());
        var usuarioSalvo = usuarioJPARepository.saveAndFlush(usuario);
        log.debug("[finish] - {} - salva", getClass().getSimpleName());
        return usuarioSalvo;
    }

    @Override
    public boolean existeUsuarioPorEmail(String email) {
        log.debug("[start] - {} - existeUsuarioPorEmail", getClass().getSimpleName());
        return usuarioJPARepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existeUsuarioPorCpf(String cpf) {
        log.debug("[start] - {} - existeUsuarioPorCpf", getClass().getSimpleName());
        return usuarioJPARepository.existsByCpf(cpf);
    }

    @Override
    public Usuario buscaUsuarioPorId(Long id) {
        log.debug("[start] - {} - buscaUsuarioPorId", getClass().getSimpleName());
        Usuario usuarioEncontrada = usuarioJPARepository
                .findById(id)
                .orElseThrow(() -> new NegocioException(CodigoErro.USUARIO_NAO_ENCONTRADO, String.valueOf(id)));
        log.debug("[finish] - {} - buscaUsuarioPorId", getClass().getSimpleName());
        return usuarioEncontrada;
    }

    @Override
    public Page<Usuario> listaUsuarios(Pageable pageable) {
        log.debug("[start] - {} - listaUsuarios", getClass().getSimpleName());
        Page<Usuario> usuarios = usuarioJPARepository.findAll(pageable);
        log.debug("[finish] - {} - listaUsuarios", getClass().getSimpleName());
        return usuarios;
    }

    @Override
    public boolean existeOutroUsuarioPorEmail(String emailLimpo, Long id) {
        log.debug("[start] - {} - existeOutroUsuarioPorEmail", getClass().getSimpleName());
        return usuarioJPARepository.existsByEmailIgnoreCaseAndIdNot(emailLimpo, id);
    }

    @Override
    public boolean existeOutroUsuarioPorCpf(String cpfSoDigitos, Long id) {
        log.debug("[start] - {} - existeOutroUsuarioPorCpf", getClass().getSimpleName());
        return usuarioJPARepository.existsByCpfAndIdNot(cpfSoDigitos, id);
    }
}
