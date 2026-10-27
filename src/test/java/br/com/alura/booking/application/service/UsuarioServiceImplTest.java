package br.com.alura.booking.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.alura.booking.api.usuario.request.UsuarioRequest;
import br.com.alura.booking.api.usuario.response.UsuarioResponse;
import br.com.alura.booking.application.repository.UsuarioRepository;
import br.com.alura.booking.domain.Usuario;
import br.com.alura.booking.exception.CodigoErro;
import br.com.alura.booking.exception.NegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void cadastraUsuarioGuardaNomeEmailECpfNormalizados() {
        var pedido = new UsuarioRequest("  Maria Souza ", " Maria@Exemplo.COM ", "529.982.247-25");
        when(usuarioRepository.salva(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse resposta = usuarioService.cadastraUsuario(pedido);

        var salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).salva(salvo.capture());
        assertThat(salvo.getValue().getNome()).isEqualTo("Maria Souza");
        assertThat(salvo.getValue().getEmail()).isEqualTo("maria@exemplo.com");
        assertThat(salvo.getValue().getCpf()).isEqualTo("52998224725");
        assertThat(resposta.getNome()).isEqualTo("Maria Souza");
        assertThat(resposta.getEmail()).isEqualTo("maria@exemplo.com");
    }

    @Test
    void cadastraUsuarioRecusaEmailJaCadastrado() {
        var pedido = new UsuarioRequest("Maria Souza", "Maria@Exemplo.com", "52998224725");
        when(usuarioRepository.existeUsuarioPorEmail("maria@exemplo.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastraUsuario(pedido))
                .isInstanceOfSatisfying(NegocioException.class, e -> assertThat(e.getCodigo())
                        .isEqualTo(CodigoErro.USUARIO_EMAIL_DUPLICADO));
        verify(usuarioRepository, never()).salva(any());
    }

    @Test
    void cadastraUsuarioRecusaCpfJaCadastrado() {
        var pedido = new UsuarioRequest("Maria Souza", "maria@exemplo.com", "529.982.247-25");
        when(usuarioRepository.existeUsuarioPorCpf("52998224725")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastraUsuario(pedido))
                .isInstanceOfSatisfying(NegocioException.class, e -> assertThat(e.getCodigo())
                        .isEqualTo(CodigoErro.USUARIO_CPF_DUPLICADO));
        verify(usuarioRepository, never()).salva(any());
    }
}
