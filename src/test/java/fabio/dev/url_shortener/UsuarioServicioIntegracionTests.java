package fabio.dev.url_shortener;

import fabio.dev.url_shortener.dtos.UsuarioRespuesta;
import fabio.dev.url_shortener.modelos.Usuario;
import fabio.dev.url_shortener.repositorios.UsuarioRepositorio;
import fabio.dev.url_shortener.servicios.UsuarioServicio;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class UsuarioServicioIntegracionTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Registrar usuario persiste el usuario real y retorna el dto sin contraseña")
    void registrarUsuario_deberiaPersistirYRetornarDto() {

        String email = "registro-integracion@test.com";
        String password = "Test1234!";

        UsuarioRespuesta respuesta = usuarioServicio.registrarUsuario(email, password);

        assertNotNull(respuesta.id());
        assertEquals(email, respuesta.email());
        assertEquals("ROLE_USER", respuesta.role());
        assertTrue(respuesta.enabled());

        Usuario guardado = usuarioRepositorio.findByEmail(email).orElseThrow();
        assertEquals(email, guardado.getEmail());
        assertNotEquals(password, guardado.getPassword());
        assertTrue(passwordEncoder.matches(password, guardado.getPassword()));
        assertEquals("ROLE_USER", guardado.getRole());
        assertTrue(guardado.getEnabled());
    }

    @Test
    @DisplayName("Registrar un email duplicado no crea otro usuario")
    void registrarUsuario_conEmailDuplicado_noDeberiaPersistir() {

        String email = "duplicado-integracion@test.com";
        usuarioServicio.registrarUsuario(email, "Test1234!");

        assertThrows(IllegalArgumentException.class,
                () -> usuarioServicio.registrarUsuario(email, "Otra1234!"));

        assertEquals(1, usuarioRepositorio.findAll().stream()
                .filter(usuario -> email.equals(usuario.getEmail()))
                .count());
    }

}
