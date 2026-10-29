package br.com.alura.booking.usuario.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@ToString(exclude = "cpf")
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {

    @NotBlank(message = "O nome do usuário é obrigatório.")
    @Size(min = 3, max = 100, message = "O nome deve ter entre {min} e {max} caracteres.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail informado é inválido.")
    @Size(max = 255, message = "O e-mail deve ter no máximo {max} caracteres.")
    private String email;

    @NotBlank(message = "O CPF é obrigatório.")
    @CPF(message = "O CPF informado é inválido.")
    private String cpf;
}
