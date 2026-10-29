package br.com.alura.booking.usuario.api.response;

import br.com.alura.booking.usuario.domain.StatusUsuario;
import br.com.alura.booking.usuario.domain.Usuario;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"id", "nome", "email", "status"})
public class UsuarioResumoResponse {

    private final Long id;
    private final String nome;
    private final String email;
    private final StatusUsuario status;

    public UsuarioResumoResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.status = usuario.getStatus();
    }
}
