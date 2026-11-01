package br.com.alura.booking.reserva.infra;

import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.reserva.domain.StatusReserva;
import org.springframework.data.jpa.domain.Specification;

final class ReservaSpecs {

    private ReservaSpecs() {}

    static Specification<Reserva> comStatus(StatusReserva status) {
        return (reserva, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(reserva.get("status"), status);
        };
    }

    static Specification<Reserva> daSala(Long idSala) {
        return (reserva, query, cb) -> {
            if (idSala == null) {
                return cb.conjunction();
            }
            return cb.equal(reserva.get("sala").get("id"), idSala);
        };
    }

    static Specification<Reserva> doUsuario(Long idUsuario) {
        return (reserva, query, cb) -> {
            if (idUsuario == null) {
                return cb.conjunction();
            }
            return cb.equal(reserva.get("usuario").get("id"), idUsuario);
        };
    }
}
