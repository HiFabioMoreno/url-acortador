package fabio.dev.url_shortener;

import fabio.dev.url_shortener.config.SecurityConfig;
import fabio.dev.url_shortener.controladores.UrlControlador;
import fabio.dev.url_shortener.dtos.ActualizarRespuesta;
import fabio.dev.url_shortener.dtos.UrlRespuesta;
import fabio.dev.url_shortener.dtos.UrlSolicitud;
import fabio.dev.url_shortener.modelos.Url;
import fabio.dev.url_shortener.servicios.UrlServicio;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlControlador.class)
@Import(SecurityConfig.class)
public class UrlControladorTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlServicio urlServicio;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private Url url;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        url = new Url();
        url.setId(1);
        url.setOriginalUrl("https://www.google.com");
        url.setSlug("uzdkr8");
        url.setTitulo("Youtube busqueda");
        url.setFechaRegistro(LocalDate.now());
        url.setFechaModificacion(LocalDate.now());
        url.setClicks(0L);

    }

    @Test
    void listarUrls_DebeRetornarLasUrlsDelUsuarioAutenticado() throws Exception {

        UrlRespuesta respuesta = new UrlRespuesta(
                url.getId(),
                url.getOriginalUrl(),
                url.getTitulo(),
                url.getSlug(),
                url.getFechaRegistro(),
                url.getFechaModificacion(),
                url.getClicks()
        );

        when(urlServicio.ListarUrls(any(Jwt.class))).thenReturn(new ArrayList<>(List.of(respuesta)));

        mockMvc.perform(get("/acortador/")
                        .with(jwt().jwt(j -> j.subject("user-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("uzdkr8"))
                .andExpect(jsonPath("$[0].originalUrl").value("https://www.google.com"));

        verify(urlServicio, times(1)).ListarUrls(any(Jwt.class));
    }

    @Test
    void registrarUrl_usuarioNoAutenticado_DeberRetornar401() throws Exception {

        UrlRespuesta respuesta = new UrlRespuesta(
                url.getId(),
                url.getOriginalUrl(),
                url.getTitulo(),
                url.getSlug(),
                url.getFechaRegistro(),
                url.getFechaModificacion(),
                url.getClicks()
        );

        mockMvc.perform(post("/acortador/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuesta)))
                .andExpect(status().isUnauthorized());

        verify(urlServicio, never()).crearShortUrl(any(UrlSolicitud.class), any(Jwt.class));
    }

    @Test
    void registrarUrl_DebeRetornar201ConUrlCreada() throws Exception {
        UrlRespuesta respuesta = new UrlRespuesta(
                url.getId(),
                url.getOriginalUrl(),
                url.getTitulo(),
                url.getSlug(),
                url.getFechaRegistro(),
                url.getFechaModificacion(),
                url.getClicks()
        );

        UrlSolicitud urlSolicitud = new UrlSolicitud("https://www.google.com");

        when(urlServicio.crearShortUrl(any(UrlSolicitud.class), any(Jwt.class))).thenReturn(respuesta);

        mockMvc.perform(post("/acortador/")
                        .with(jwt().jwt(j -> j.subject("user-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlSolicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("uzdkr8"))
                .andExpect(jsonPath("$.originalUrl").value("https://www.google.com"));

        verify(urlServicio, times(1)).crearShortUrl(any(UrlSolicitud.class), any(Jwt.class));
    }

    @Test
    void registrarUrl_CuandoUrlEsVAcio_DebeRetornar400() throws Exception {

        String body = """
            {
                "url": ""
            }
            """;

        mockMvc.perform(post("/acortador/")
                        .with(jwt().jwt(j -> j.subject("user-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errorCode").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registrarUrl_CuandoUrlVacio_DebeRetornar400() throws Exception {

        String body = "";

        mockMvc.perform(post("/acortador/")
                        .with(jwt().jwt(j -> j.subject("user-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errorCode").value("URL_NO_VALIDA"))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void actualizarUrl_DebeRetornar200ConUrlActualizada() throws Exception {

        ActualizarRespuesta urlActualizar = new ActualizarRespuesta("YouTube");

        UrlRespuesta respuesta = new UrlRespuesta(
                1,
                url.getOriginalUrl(),
                urlActualizar.titulo(),
                url.getSlug(),
                url.getFechaRegistro(),
                url.getFechaModificacion(),
                url.getClicks()
        );

        when(urlServicio.actualizarShortUrl(eq(1), eq(urlActualizar), any(Jwt.class))).thenReturn(respuesta);

        mockMvc.perform(patch("/acortador/1/")
                        .with(jwt().jwt(j -> j.subject("user-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(urlActualizar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("uzdkr8"))
                .andExpect(jsonPath("$.titulo").value("YouTube"));
    }

    @Test
    void eliminarUrl_DebeRetornar200ConUrlEliminada() throws Exception {

        doNothing().when(urlServicio).EliminarUrl(eq(1), any(Jwt.class));
        mockMvc.perform(delete("/acortador/1/")
                .with(jwt().jwt(j -> j.subject("user-123"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarUrl_CuandoNoExiste_DebeRetornar404() throws Exception {
        doThrow(new EntityNotFoundException("URL con id 99 no existe"))
                .when(urlServicio).EliminarUrl(eq(99), any(Jwt.class));

        mockMvc.perform(delete("/acortador/99/")
                        .with(jwt().jwt(j -> j.subject("user-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("URL_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

}