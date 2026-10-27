package br.com.alura.booking.api.usuario.response;

import br.com.alura.booking.domain.Usuario;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"id", "nome", "email", "cadastradoEm"})
public class UsuarioResponse {

    private final Long id;
    private final String nome;
    private final String email;
    private final LocalDateTime cadastradoEm;

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.cadastradoEm = usuario.getCadastradoEm();
    }
}
