package br.com.alura.booking.api.sala.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SalaRequest {

    @NotBlank(message = "O nome da sala é obrigatório.")
    @Size(min = 3, max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nome;

    @NotNull(message = "A capacidade é obrigatória.")
    @Min(value = 1)
    @Max(value = 50)
    private Integer capacidade;
}
