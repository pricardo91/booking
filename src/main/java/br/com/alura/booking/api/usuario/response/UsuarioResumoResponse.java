package br.com.alura.booking.api.usuario.response;

import br.com.alura.booking.domain.Usuario;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"id", "nome", "email", "cadastradoEm"})
public class UsuarioResumoResponse {

    private Long id;
    private String nome;
    private String email;

    public UsuarioResumoResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
    }
}
