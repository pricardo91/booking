package br.com.alura.booking.reserva.domain;

import br.com.alura.booking.sala.domain.Sala;
import br.com.alura.booking.shared.exception.CodigoErro;
import br.com.alura.booking.shared.exception.NegocioException;
import br.com.alura.booking.usuario.domain.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Ocupação de uma Sala por um Usuário em um intervalo de tempo. É a entidade central do sistema:
 * liga quem (Usuario), o quê (Sala) e quando (inicio/fim).
 *
 * <p>Política de conflito: a mesma sala não pode ter duas reservas ATIVAS sobrepostas. O intervalo
 * é semiaberto, [inicio, fim): inclui o início e não inclui o fim. Duas reservas A e B da mesma
 * sala conflitam quando A.inicio &lt; B.fim e B.inicio &lt; A.fim.
 *
 * <ul>
 *   <li>10h–11h e 11h–12h: NÃO conflitam (o fim de uma é igual ao início da outra).
 *   <li>10h–11h e 10h30–11h30: conflitam (as duas ocupam a sala entre 10h30 e 11h).
 *   <li>10h–12h e 10h30–11h: conflitam (uma está contida na outra).
 * </ul>
 *
 * <p>Só reservas ATIVAS ocupam a sala. PENDENTE (aguardando confirmação) não bloqueia o horário, e
 * CANCELADA e FINALIZADA também não. Quando uma reserva é confirmada vale "o primeiro a ser confirmado ganha": o
 * serviço cancela as outras PENDENTES da mesma sala que se sobrepõem a ela.
 *
 * <p>Esta classe só responde perguntas sobre si mesma (sobrepoe, ocupaSala). Buscar as outras
 * reservas no banco e decidir o que fazer com elas é papel do serviço.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reserva {

    /**
     * Identificador gerado pelo banco. Long porque o null indica "reserva ainda não salva".
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long id;

    /**
     * Quem reservou. Muitas reservas para um usuário. Obrigatório, porque uma reserva sem
     * responsável não pode ser confirmada nem cancelada por ninguém. LAZY evita carregar o usuário
     * sem necessidade.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * O que foi reservado. Muitas reservas para uma sala ao longo do tempo, em intervalos que não
     * podem se sobrepor. Obrigatório, porque o conflito de horário só existe em relação a uma sala.
     * LAZY evita carregar a sala sem necessidade.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sala", nullable = false)
    private Sala sala;

    /**
     * Situação da reserva no seu ciclo de vida (veja {@link StatusReserva}). Define se ela ocupa a
     * sala: só ATIVO ocupa (veja {@link #ocupaSala()}). Nasce PENDENTE e só muda por confirmar(),
     * cancelar() e finalizar(). Gravado como texto para não depender da ordem do enum no banco.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusReserva status = StatusReserva.PENDENTE;

    /**
     * Início da ocupação, incluído no intervalo. Obrigatório, porque sem ele não há como saber se
     * duas reservas conflitam.
     */
    @Column(nullable = false)
    private LocalDateTime inicio;

    /**
     * Fim da ocupação, NÃO incluído no intervalo (intervalo semiaberto). Por isso o fim de uma
     * reserva pode coincidir com o início da próxima. Deve ser posterior ao início. Obrigatório.
     */
    @Column(nullable = false)
    private LocalDateTime fim;

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
     * Cria uma reserva PENDENTE. Todos os campos são obrigatórios, a sala e o usuário precisam estar
     * ativos e o fim precisa ser posterior ao início. As regras são conferidas nesta ordem: campos,
     * sala, intervalo e usuário; a primeira que falhar interrompe a criação.
     *
     * @throws NegocioException (CAMPO_OBRIGATORIO, SALA_INATIVA, RESERVA_INTERVALO_INVALIDO ou
     *     USUARIO_INATIVO) se algum campo for nulo, a sala estiver inativa, o intervalo for inválido
     *     ou o usuário estiver inativo
     */
    public Reserva(Usuario usuario, Sala sala, LocalDateTime inicio, LocalDateTime fim) {
        this.usuario = exigir(usuario, "usuário");
        this.sala = exigir(sala, "sala");
        this.inicio = exigir(inicio, "início");
        this.fim = exigir(fim, "fim");

        validaSalaAtiva();
        validaIntervalo();
        validaUsuarioAtivo();
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
     * Devolve o próprio valor se ele existir; se for nulo, interrompe com o erro de campo
     * obrigatório, citando o nome do campo.
     */
    private static <T> T exigir(T valor, String campo) {
        if (valor == null) {
            throw new NegocioException(CodigoErro.CAMPO_OBRIGATORIO, campo);
        }
        return valor;
    }

    /**
     * Confirma a reserva (PENDENTE para ATIVO). Cancelar as outras pendentes que disputam o mesmo
     * horário não é feito aqui, e sim pelo serviço.
     *
     * @throws NegocioException (RESERVA_SO_PENDENTE_CONFIRMA) se a reserva não estiver PENDENTE
     */
    public void confirmar() {
        if (this.status != StatusReserva.PENDENTE) {
            throw new NegocioException(CodigoErro.RESERVA_SO_PENDENTE_CONFIRMA);
        }
        this.status = StatusReserva.ATIVO;
    }

    /**
     * Cancela a reserva (de PENDENTE ou ATIVO para CANCELADO). CANCELADO é estado final.
     *
     * @throws NegocioException (RESERVA_JA_CANCELADA ou RESERVA_FINALIZADA_NAO_CANCELA) se a reserva
     *     já estiver cancelada ou já tiver sido finalizada
     */
    public void cancelar() {
        if (this.status == StatusReserva.CANCELADO) {
            throw new NegocioException(CodigoErro.RESERVA_JA_CANCELADA);
        }
        if (this.status == StatusReserva.FINALIZADO) {
            throw new NegocioException(CodigoErro.RESERVA_FINALIZADA_NAO_CANCELA);
        }
        this.status = StatusReserva.CANCELADO;
    }

    /**
     * Finaliza a reserva (ATIVO para FINALIZADO) quando o seu horário terminou. FINALIZADO é estado
     * final. Recebe o instante atual como parâmetro, em vez de consultar o relógio, para a regra ser
     * previsível e testável. Quem chama no momento certo é o serviço (por exemplo, um job agendado).
     *
     * @param agora o instante atual; no instante exato do fim a reserva já pode ser finalizada,
     *     porque o fim não faz parte do intervalo
     * @throws NegocioException (RESERVA_SO_ATIVA_FINALIZA) se a reserva não estiver ATIVO
     * @throws NegocioException (CAMPO_OBRIGATORIO ou RESERVA_AINDA_NAO_TERMINOU) se o instante for
     *     nulo ou o horário ainda não terminou
     */
    public void finalizar(LocalDateTime agora) {
        exigir(agora, "instante atual");
        if (this.status != StatusReserva.ATIVO) {
            throw new NegocioException(CodigoErro.RESERVA_SO_ATIVA_FINALIZA);
        }
        if (agora.isBefore(fim)) {
            throw new NegocioException(CodigoErro.RESERVA_AINDA_NAO_TERMINOU);
        }
        this.status = StatusReserva.FINALIZADO;
    }

    /**
     * Indica se esta reserva bloqueia a sala. Só ATIVO bloqueia; PENDENTE, CANCELADO e FINALIZADO
     * não.
     */
    public boolean ocupaSala() {
        return status == StatusReserva.ATIVO;
    }

    /**
     * Recusa reservar uma sala inativa.
     */
    private void validaSalaAtiva() {
        if (!sala.isAtiva()) {
            throw new NegocioException(CodigoErro.SALA_INATIVA);
        }
    }

    /**
     * Recusa um intervalo em que o fim não é posterior ao início.
     */
    private void validaIntervalo() {
        if (!isIntervaloValido()) {
            throw new NegocioException(CodigoErro.RESERVA_INTERVALO_INVALIDO);
        }
    }

    /**
     * Regra de domínio: o fim precisa ser posterior ao início (duração zero não é aceita, porque um
     * intervalo [inicio, inicio) é vazio e não ocuparia a sala). Campos nulos retornam true: a
     * obrigatoriedade é verificada pelo construtor.
     */
    private boolean isIntervaloValido() {
        return inicio == null || fim == null || fim.isAfter(inicio);
    }

    /**
     * Recusa uma reserva feita por um usuário inativo.
     */
    private void validaUsuarioAtivo() {
        if (!usuario.isAtivo()) {
            throw new NegocioException(CodigoErro.USUARIO_INATIVO);
        }
    }

    /**
     * Indica se esta reserva e a outra disputam o mesmo horário na mesma sala: a sala é a mesma e os
     * horários se cruzam, no intervalo semiaberto [inicio, fim). Se o fim de uma for igual ao início
     * da outra, NÃO se cruzam. Não olha o status; quem decide o que fazer com cada status é o
     * serviço. Da outra reserva usa os getters (e não os campos) porque ela pode ser um proxy do
     * Hibernate, cujos campos podem estar vazios.
     */
    public boolean sobrepoe(Reserva outra) {
        return this.sala.equals(outra.getSala())
                && this.inicio.isBefore(outra.getFim())
                && outra.getInicio().isBefore(this.fim);
    }
}
