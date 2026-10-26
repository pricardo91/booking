package br.com.alura.booking.api.sala;

import br.com.alura.booking.api.sala.request.SalaRequest;
import br.com.alura.booking.api.sala.response.PageResponse;
import br.com.alura.booking.api.sala.response.SalaResponse;
import br.com.alura.booking.api.sala.response.SalaResumoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/salas")
public interface SalaAPI {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SalaResponse cadastraSala(@Valid @RequestBody SalaRequest novaSala);

    @GetMapping
    PageResponse<SalaResumoResponse> listaSala(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(100) int size);

    @GetMapping("/{id}")
    SalaResponse buscaSalaPorId(@PathVariable long id);

    @PutMapping("/{id}")
    void atualizaSala(@PathVariable long id, @Valid @RequestBody SalaRequest atualizaSala);

    @PatchMapping("/{id}/inativar")
    void inativaSala(@PathVariable long id);

    @PatchMapping("/{id}/ativar")
    void ativaSala(@PathVariable long id);
}
