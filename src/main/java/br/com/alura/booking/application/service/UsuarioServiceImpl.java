package br.com.alura.booking.application.service;

import br.com.alura.booking.api.usuario.request.UsuarioRequest;
import br.com.alura.booking.api.usuario.response.UsuarioResponse;
import br.com.alura.booking.application.repository.UsuarioRepository;
import br.com.alura.booking.domain.Usuario;
import br.com.alura.booking.exception.CodigoErro;
import br.com.alura.booking.exception.NegocioException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        var nomeLimpo = novoUsuario.getNome().strip();
        var emailLimpo = novoUsuario.getEmail().strip().toLowerCase(Locale.ROOT);
        var cpfSoDigitos = novoUsuario.getCpf().replaceAll("\\D", "");

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
}
