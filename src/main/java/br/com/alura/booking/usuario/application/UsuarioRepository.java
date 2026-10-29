package br.com.alura.booking.usuario.application;

import br.com.alura.booking.usuario.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioRepository {
    Usuario salva(Usuario usuario);

    boolean existeUsuarioPorEmail(String email);

    boolean existeUsuarioPorCpf(String cpf);

    Usuario buscaUsuarioPorId(Long id);

    Page<Usuario> listaUsuarios(Pageable pageable);

    boolean existeOutroUsuarioPorEmail(String emailLimpo, Long id);

    boolean existeOutroUsuarioPorCpf(String cpfSoDigitos, Long id);
}
