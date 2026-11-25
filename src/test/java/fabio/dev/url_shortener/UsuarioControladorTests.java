package fabio.dev.url_shortener;

import fabio.dev.url_shortener.config.SecurityConfig;
import fabio.dev.url_shortener.controladores.UsuarioControlador;
import fabio.dev.url_shortener.dtos.UsuarioRespuesta;
import fabio.dev.url_shortener.servicios.UsuarioServicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioControlador.class)
@Import(SecurityConfig.class)
class UsuarioControladorTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioServicio usuarioServicio;

    @Test
    @DisplayName("Registrar usuario sin autenticacion ni token CSRF esta permitido")
    void registrarUsuario_sinAutenticacion_deberiaCrearUsuario() throws Exception {

        when(usuarioServicio.registrarUsuario("nuevo@test.com", "Test1234!"))
                .thenReturn(new UsuarioRespuesta(1L, "nuevo@test.com", "ROLE_USER", true));

        mockMvc.perform(post("/registro/")
                        .param("email", "nuevo@test.com")
                        .param("password", "Test1234!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("nuevo@test.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(usuarioServicio).registrarUsuario("nuevo@test.com", "Test1234!");
    }

    @Test
    @DisplayName("Las demas rutas siguen requiriendo autenticacion")
    void acortador_sinAutenticacion_deberiaRechazarAcceso() throws Exception {

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/acortador/"))
                .andExpect(status().isUnauthorized());
    }

}
