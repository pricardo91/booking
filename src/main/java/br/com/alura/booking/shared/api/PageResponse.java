package br.com.alura.booking.shared.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private Long totalElements;
    private int paginaAtual;
    private int totalPaginas;

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getNumber(), page.getTotalPages());
    }
}
