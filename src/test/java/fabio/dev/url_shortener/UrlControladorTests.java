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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlControlador.class)
@Import(SecurityConfig.class)
public class UrlControladorTests {

    private static final String EMAIL = "user@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlServicio urlServicio;

    @Autowired
    private ObjectMapper objectMapper;

    private Url url;

    @BeforeEach
    void setUp() {

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
    void listarUrls_DebeRetornarLasUrlsDelUsuarioAutenticadoConPageable() throws Exception {

        UrlRespuesta respuesta = new UrlRespuesta(
                url.getId(),
                url.getOriginalUrl(),
                url.getTitulo(),
                url.getSlug(),
                url.getFechaRegistro(),
                url.getFechaModificacion(),
                url.getClicks()
        );

        when(urlServicio.ListarUrls(eq(EMAIL), any(Pageable.class)))
                .thenReturn(new ArrayList<>(List.of(respuesta)));

        mockMvc.perform(get("/acortador/")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "slug,desc")
                        .with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("uzdkr8"))
                .andExpect(jsonPath("$[0].originalUrl").value("https://www.google.com"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(urlServicio, times(1)).ListarUrls(eq(EMAIL), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("slug").getDirection());
    }

    @Test
    void listarUrls_DebeAceptarOrdenAscendente() throws Exception {

        when(urlServicio.ListarUrls(eq(EMAIL), any(Pageable.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/acortador/")
                        .param("sort", "slug,asc")
                        .with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(urlServicio).ListarUrls(eq(EMAIL), pageableCaptor.capture());
        assertEquals(Sort.Direction.ASC, pageableCaptor.getValue().getSort().getOrderFor("slug").getDirection());
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuesta)))
                .andExpect(status().isUnauthorized());

        verify(urlServicio, never()).crearShortUrl(any(UrlSolicitud.class), anyString());
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

        when(urlServicio.crearShortUrl(any(UrlSolicitud.class), eq(EMAIL))).thenReturn(respuesta);

        mockMvc.perform(post("/acortador/")
                        .with(user(EMAIL).roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlSolicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("uzdkr8"))
                .andExpect(jsonPath("$.originalUrl").value("https://www.google.com"));

        verify(urlServicio, times(1)).crearShortUrl(any(UrlSolicitud.class), eq(EMAIL));
    }

    @Test
    void registrarUrl_CuandoUrlEsVAcio_DebeRetornar400() throws Exception {

        String body = """
            {
                "url": ""
            }
            """;

        mockMvc.perform(post("/acortador/")
                        .with(user(EMAIL).roles("USER"))
                        .with(csrf())
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
                        .with(user(EMAIL).roles("USER"))
                        .with(csrf())
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
    void registrarUrl_CuandoHayViolacionDeIntegridad_DebeRetornar409() throws Exception {

        UrlSolicitud urlSolicitud = new UrlSolicitud("https://www.google.com");

        when(urlServicio.crearShortUrl(any(UrlSolicitud.class), eq(EMAIL)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key value violates unique constraint \"uk_url_usuario_original_url\""));

        mockMvc.perform(post("/acortador/")
                        .with(user(EMAIL).roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlSolicitud)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.errorCode").value("URL_DUPLICADA"))
                .andExpect(jsonPath("$.mensaje").value("El recurso ya existe o viola una restriccion de unicidad"))
                .andExpect(jsonPath("$.mensajeDesarrollador").value(containsString("uk_url_usuario_original_url")))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(urlServicio, times(1)).crearShortUrl(any(UrlSolicitud.class), eq(EMAIL));
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

        when(urlServicio.actualizarShortUrl(eq(1), eq(urlActualizar), eq(EMAIL))).thenReturn(respuesta);

        mockMvc.perform(patch("/acortador/1/")
                        .with(user(EMAIL).roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlActualizar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("uzdkr8"))
                .andExpect(jsonPath("$.titulo").value("YouTube"));
    }

    @Test
    void eliminarUrl_DebeRetornar200ConUrlEliminada() throws Exception {

        doNothing().when(urlServicio).EliminarUrl(eq(1), eq(EMAIL));
        mockMvc.perform(delete("/acortador/1/")
                        .with(user(EMAIL).roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarUrl_CuandoNoExiste_DebeRetornar404() throws Exception {

        doThrow(new EntityNotFoundException("URL con id 99 no existe"))
                .when(urlServicio).EliminarUrl(eq(99), eq(EMAIL));

        mockMvc.perform(delete("/acortador/99/")
                        .with(user(EMAIL).roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("URL_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
