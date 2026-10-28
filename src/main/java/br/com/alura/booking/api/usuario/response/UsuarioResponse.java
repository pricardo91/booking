package br.com.alura.booking.api.usuario.response;

import br.com.alura.booking.domain.Usuario;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"id", "nome", "email", "cadastradoEm", "atualizadoEm"})
public class UsuarioResponse {

    private final Long id;
    private final String nome;
    private final String email;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private final LocalDateTime cadastradoEm;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private final LocalDateTime atualizadoEm;

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.cadastradoEm = usuario.getCadastradoEm();
        this.atualizadoEm = usuario.getAtualizadoEm();
    }
}
