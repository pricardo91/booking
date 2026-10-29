package br.com.alura.booking.usuario.domain;

import br.com.alura.booking.reserva.domain.Reserva;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pessoa que faz reservas. Existe como entidade própria para que cada reserva tenha um responsável
 * identificável, que pode ser contatado e que responde pelo uso da sala.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario {

    /**
     * Identificador gerado pelo banco. Long (e não long) porque o null indica "usuário ainda não
     * salvo".
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    /**
     * Nome pelo qual a pessoa aparece nas reservas. Obrigatório; o limite de 100 caracteres define o
     * tamanho da coluna. A validação é feita na entrada da API (DTO).
     */
    @Setter
    @Column(nullable = false, length = 100)
    private String nome;

    /**
     * Canal de contato para confirmações e avisos de cancelamento. Obrigatório e único (garantido
     * pela coluna), para identificar a pessoa. O formato é validado na entrada da API (DTO).
     */
    @Setter
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Documento da pessoa, guardado só com os 11 dígitos. É dado pessoal (LGPD): só deve existir
     * enquanto houver motivo real para guardá-lo. É obrigatório e único porque identifica uma pessoa
     * só. O formato e o dígito verificador são validados na entrada da API (DTO), não aqui.
     */
    @Setter
    @Column(unique = true, length = 11, nullable = false)
    private String cpf;

    /**
     * Indica se o usuário pode fazer reservas novas. Regra de domínio: usuário INATIVO não deve fazer
     * reservas. Inativar o usuário (em vez de apagá-lo) preserva o histórico das reservas que ele já
     * fez. Nasce ATIVO. Gravado como texto para não depender da ordem do enum no banco.
     *
     * <p>A regra ainda não é aplicada: deve ser conferida no construtor de {@link Reserva}, como já
     * acontece com a sala inativa.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusUsuario status = StatusUsuario.ATIVO;

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
     * Cria um usuário. Os dados são validados na entrada da API (DTO); as datas de cadastro e
     * atualização são preenchidas pelo sistema.
     */
    public Usuario(String nome, String email, String cpf) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
    }

    /** Preenche a data de cadastro antes de gravar pela primeira vez. */
    @PrePersist
    protected void onCreation() {
        cadastradoEm = LocalDateTime.now();
    }

    /** Preenche a data da última alteração a cada atualização. */
    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    /**
     * Volta a poder fazer reservas novas.
     *
     * @throws NegocioException (USUARIO_JA_ATIVO) se o usuário já estiver ativo
     */
    public void ativar() {
        if (status != StatusUsuario.INATIVO) {
            throw new NegocioException(CodigoErro.USUARIO_JA_ATIVO);
        }
        this.status = StatusUsuario.ATIVO;
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Deixa de poder fazer reservas novas. As reservas que já existem não são alteradas.
     *
     * @throws NegocioException (USUARIO_JA_INATIVO) se o usuário já estiver inativo
     */
    public void inativar() {
        if (status != StatusUsuario.ATIVO) {
            throw new NegocioException(CodigoErro.USUARIO_JA_INATIVO);
        }
        this.status = StatusUsuario.INATIVO;
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Indica se o usuário pode fazer reservas novas.
     */
    public boolean isAtivo() {
        return status == StatusUsuario.ATIVO;
    }

    /**
     * Atualiza nome, e-mail e CPF e registra o momento da alteração. O formato dos dados é validado na
     * entrada da API (DTO); que o e-mail e o CPF não pertençam a outro usuário é conferido pelo
     * serviço. Os valores chegam já normalizados (nome sem espaços nas pontas, e-mail em minúsculas e
     * CPF só com dígitos).
     */
    public void atualizarUsuario(String nomeAtualizado, String emailAtualizado, String cpfAtualizado) {
        this.nome = nomeAtualizado;
        this.email = emailAtualizado;
        this.cpf = cpfAtualizado;
        this.atualizadoEm = LocalDateTime.now();
    }
}
