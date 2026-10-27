package br.com.alura.booking.infra;

import br.com.alura.booking.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJPARepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);
}
