package br.com.alura.booking.application.repository;

import br.com.alura.booking.domain.Usuario;

public interface UsuarioRepository {
    Usuario salva(Usuario usuario);

    boolean existeUsuarioPorEmail(String email);

    boolean existeUsuarioPorCpf(String cpf);

    Usuario buscaUsuarioPorId(Long id);
}
