package br.com.alura.booking.sala.domain;

import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Espaço físico que pode ser reservado. É o recurso disputado pelas reservas: a regra de não
 * sobreposição é sempre avaliada por sala.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sala {

    /**
     * Identificador gerado pelo banco (IDENTITY). É Long, e não long, porque uma sala nova ainda não
     * tem id: o null representa "ainda não salva", enquanto o primitivo viraria 0 e confundiria.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sala")
    private Long id;

    /**
     * Como as pessoas reconhecem a sala (ex.: "Sala Azul"). Obrigatório, porque é o que aparece nas
     * listagens. O limite de 100 caracteres define o tamanho da coluna; o formato e o tamanho são
     * validados na entrada da API (DTO), não aqui.
     */
    @Column(nullable = false, length = 100, unique = true)
    private String nome;

    /**
     * Quantas pessoas cabem na sala. Regra de domínio: deve ser positiva, já que uma sala sem lugares
     * não pode ser reservada por ninguém. É primitivo porque nunca deve ficar sem valor.
     */
    @Column(nullable = false)
    @Min(value = 1)
    private int capacidade;

    /**
     * Indica se a sala aceita reservas novas. Regra de domínio: sala INATIVA não pode ser reservada.
     * Inativar a sala (em vez de apagá-la) preserva o histórico das reservas que já a usaram. Nasce
     * ATIVA. Gravado como texto para não depender da ordem do enum no banco.
     *
     * <p>A regra é aplicada no construtor de {@link Reserva}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSala status = StatusSala.ATIVA;

    /**
     * Momento do cadastro. Preenchido pelo sistema em onCreation(), nunca pelo cliente, para servir
     * de registro confiável de auditoria.
     */
    @Column(nullable = false, name = "cadastrado_em")
    private LocalDateTime cadastradoEm;

    /**
     * Momento da última alteração. Preenchido pelo sistema em onUpdate(). Fica vazio até a primeira
     * alteração depois do cadastro.
     */
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    /**
     * Cria uma sala ativa. A capacidade deve ser positiva; o nome é validado na entrada da API
     * (DTO).
     *
     * @throws NegocioException (SALA_CAPACIDADE_INVALIDA) se a capacidade não for positiva
     */
    public Sala(String nome, int capacidade) {
        setCapacidade(capacidade);
        this.nome = nome;
    }

    /**
     * Preenche a data de cadastro antes de gravar pela primeira vez.
     */
    @PrePersist
    protected void onCreation() {
        cadastradoEm = LocalDateTime.now();
    }

    /**
     * Preenche a data da última alteração a cada atualização.
     */
    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public void setCapacidade(int capacidade) {
        if (capacidade <= 0) {
            throw new NegocioException(CodigoErro.SALA_CAPACIDADE_INVALIDA);
        }
        this.capacidade = capacidade;
    }

    /**
     * Volta a aceitar reservas novas.
     *
     * @throws NegocioException (SALA_JA_ATIVA) se a sala já estiver ativa
     */
    public void ativar() {
        if (status != StatusSala.INATIVA) {
            throw new NegocioException(CodigoErro.SALA_JA_ATIVA);
        }
        this.status = StatusSala.ATIVA;
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Deixa de aceitar reservas novas. As reservas que já existem não são alteradas.
     *
     * @throws NegocioException (SALA_JA_INATIVA) se a sala já estiver inativa
     */
    public void inativar() {
        if (status != StatusSala.ATIVA) {
            throw new NegocioException(CodigoErro.SALA_JA_INATIVA);
        }
        this.status = StatusSala.INATIVA;
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Indica se a sala aceita reservas novas.
     */
    public boolean isAtiva() {
        return status == StatusSala.ATIVA;
    }

    /**
     * Atualiza o nome e a capacidade da sala. A capacidade é validada antes de qualquer alteração,
     * então, se for recusada, a sala continua exatamente como estava. O nome é validado na entrada da
     * API (DTO); que ele não repita o de outra sala é conferido pelo serviço.
     *
     * @throws NegocioException (SALA_CAPACIDADE_INVALIDA) se a capacidade não for positiva
     */
    public void atualizarSala(String nomeAtualizado, int capacidadeAtualizada) {
        setCapacidade(capacidadeAtualizada);
        this.nome = nomeAtualizado;
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Duas salas são a mesma quando têm o mesmo id. Uma sala ainda não salva (sem id) só é igual a
     * si mesma, para duas salas novas e diferentes não serem confundidas. Usa o getter na outra sala
     * porque ela pode ser um proxy do Hibernate (relação LAZY), cujo campo pode estar vazio.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sala outra)) {
            return false;
        }

        return id != null && id.equals(outra.getId());
    }

    /**
     * Fixo para a classe, e não calculado pelo id: o id muda de nulo para um número ao salvar, e o
     * hashCode de um objeto não pode mudar enquanto ele está num HashSet ou HashMap.
     */
    @Override
    public int hashCode() {
        return Sala.class.hashCode();
    }
}
