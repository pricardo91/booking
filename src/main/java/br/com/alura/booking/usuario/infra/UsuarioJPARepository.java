package br.com.alura.booking.usuario.infra;

import br.com.alura.booking.usuario.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJPARepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmailIgnoreCaseAndIdNot(String emailLimpo, Long id);

    boolean existsByCpfAndIdNot(String cpfSoDigitos, Long id);
}
