package fabio.dev.url_shortener;

import fabio.dev.url_shortener.dtos.UsuarioRespuesta;
import fabio.dev.url_shortener.modelos.Usuario;
import fabio.dev.url_shortener.repositorios.UsuarioRepositorio;
import fabio.dev.url_shortener.servicios.UsuarioServicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServicioTests {

    private static final String EMAIL = "registro@gmail.com";
    private static final String PASSWORD = "Test1234!";
    private static final String PASSWORD_ENCODED = "$2a$10$hashGenerado";

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServicio usuarioServicio;

    @Test
    @DisplayName("Deberia registrar un usuario y retornar un dto sin la entidad ni la contraseña")
    void deberiaRegistrarUsuarioRetornandoDto() {

        when(usuarioRepositorio.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_ENCODED);
        when(usuarioRepositorio.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioRespuesta respuesta = usuarioServicio.registrarUsuario(EMAIL, PASSWORD);

        assertEquals(1L, respuesta.id());
        assertEquals(EMAIL, respuesta.email());
        assertEquals("ROLE_USER", respuesta.role());
        assertTrue(respuesta.enabled());
        assertEquals(4, UsuarioRespuesta.class.getRecordComponents().length);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepositorio).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals(EMAIL, guardado.getEmail());
        assertEquals(PASSWORD_ENCODED, guardado.getPassword());
        assertEquals("ROLE_USER", guardado.getRole());
        assertTrue(guardado.getEnabled());
        assertNotEquals(PASSWORD, guardado.getPassword());

        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    @DisplayName("No deberia registrar un usuario con un email ya existente")
    void noDeberiaRegistrarUsuarioConEmailDuplicado() {

        when(usuarioRepositorio.existsByEmail(EMAIL)).thenReturn(true);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> usuarioServicio.registrarUsuario(EMAIL, PASSWORD));

        assertEquals("El email ya existe", error.getMessage());
        verify(usuarioRepositorio, never()).save(any(Usuario.class));
        verifyNoInteractions(passwordEncoder);
    }

}
