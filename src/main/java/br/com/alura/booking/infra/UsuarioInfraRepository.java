package br.com.alura.booking.infra;

import br.com.alura.booking.application.repository.UsuarioRepository;
import br.com.alura.booking.domain.Usuario;
import br.com.alura.booking.exception.CodigoErro;
import br.com.alura.booking.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UsuarioInfraRepository implements UsuarioRepository {

    private final UsuarioJPARepository usuarioJPARepository;

    @Override
    public Usuario salva(Usuario usuario) {
        log.debug("[start] - {} - salva", getClass().getSimpleName());
        var usuarioSalvo = usuarioJPARepository.save(usuario);
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
}
