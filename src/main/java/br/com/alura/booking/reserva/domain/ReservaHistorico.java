package br.com.alura.booking.reserva.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Registro de uma mudança de status de uma {@link Reserva}: de qual status para qual, por quê e
 * quando. É gravado na mesma transação da mudança, para nunca existir uma sem a outra, e não é
 * alterado depois: serve de trilha de auditoria. Por isso a classe não tem setters.
 *
 * <p>Esta classe só guarda o fato. Decidir qual é o novo status é papel da {@link Reserva}
 * (confirmar, cancelar), e gravar o histórico junto é papel do serviço.
 */
@Entity
@Table(name = "reserva_historico")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservaHistorico {

    /** Identificador gerado pelo banco. Long porque o null indica "registro ainda não salvo". */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historico")
    private Long id;

    /**
     * Reserva a que este registro pertence. Muitos registros para uma reserva, ao longo da vida
     * dela. Obrigatório, porque um histórico sem reserva não conta nada. LAZY evita carregar a
     * reserva sem necessidade.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    /**
     * Status que a reserva tinha antes da mudança. Gravado como texto, para não depender da ordem
     * do enum no banco.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", nullable = false)
    private StatusReserva statusAnterior;

    /** Status que a reserva passou a ter depois da mudança. Gravado como texto. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false)
    private StatusReserva statusNovo;

    /**
     * Por que o status mudou (veja {@link MotivoHistorico}). Permite distinguir, por exemplo, um
     * cancelamento pedido pelo usuário de um cancelamento automático por conflito de horário.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false)
    private MotivoHistorico motivo;

    /**
     * Momento em que o registro foi criado, que é também o momento da mudança, porque os dois são
     * gravados na mesma transação. Preenchido pelo sistema em onCreation(), nunca pelo cliente,
     * para servir de registro confiável de auditoria.
     */
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    /**
     * Registra uma mudança de status. Não valida os campos: eles são obrigatórios no banco (NOT
     * NULL) e quem chama é o serviço, que sempre os informa.
     *
     * @param reserva a reserva que mudou de status
     * @param statusAnterior o status antes da mudança
     * @param statusNovo o status depois da mudança
     * @param motivo por que a mudança aconteceu
     */
    public ReservaHistorico(
            Reserva reserva, StatusReserva statusAnterior, StatusReserva statusNovo, MotivoHistorico motivo) {
        this.reserva = reserva;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.motivo = motivo;
    }

    /** Preenche o momento de criação antes de gravar pela primeira vez. */
    @PrePersist
    protected void onCreation() {
        this.criadoEm = LocalDateTime.now();
    }
}
