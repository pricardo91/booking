package br.com.alura.booking.api.usuario.response;

import br.com.alura.booking.domain.Usuario;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"id", "nome", "email"})
public class UsuarioResponse {

    private final Long id;
    private final String nome;
    private final String email;

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
    }
}
