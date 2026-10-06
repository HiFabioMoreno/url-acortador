package fabio.dev.url_shortener;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlControlador.class)
public class UrlControladorTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlServicio urlServicio;

    @Autowired
    private ObjectMapper objectMapper;

    private Url url;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        url = new Url();
        url.setId(1);
        url.setOriginalUrl("https://www.google.com");
        url.setSlug("uzdkr8");
        url.setFechaRegistro(LocalDateTime.now().toString());
        url.setFechaModificacion(LocalDateTime.now().toString());
        url.setVecesAccedido(0);

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
                url.getVecesAccedido()
        );

        UrlSolicitud urlSolicitud = new UrlSolicitud("https://www.google.com");

        when(urlServicio.crearShortUrl(any(UrlSolicitud.class))).thenReturn(respuesta);

        mockMvc.perform(post("/acortador/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlSolicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("uzdkr8"))
                .andExpect(jsonPath("$.originalUrl").value("https://www.google.com"));

        verify(urlServicio, times(1)).crearShortUrl(any(UrlSolicitud.class));
    }

    @Test
    void registrarUrl_CuandoUrlEsVAcio_DebeRetornar400() throws Exception {

        String body = """
            {
                "url": ""
            }
            """;

        mockMvc.perform(post("/acortador/")
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

        ActualizarRespuesta urlActulizar = new ActualizarRespuesta(
                "https://www.youtube.com",
                "YouTube",
                false,
                false
        );

        UrlRespuesta respuesta = new UrlRespuesta(
                1,
                "https://www.youtube.com",
                url.getTitulo(),
                url.getSlug(),
                url.getFechaRegistro(),
                url.getFechaModificacion(),
                url.getVecesAccedido()
        );

        when(urlServicio.actualizarShortUrl(1, urlActulizar)).thenReturn(respuesta);

        mockMvc.perform(patch("/acortador/1/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(urlActulizar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("uzdkr8"))
                .andExpect(jsonPath("$.originalUrl").value("https://www.youtube.com"));
    }

    @Test
    void actualizarUrl_CuandoUrlInvalida_DebeRetornar400() throws Exception {

        ActualizarRespuesta body = new ActualizarRespuesta("ftp://invalida", null, false, false);

        mockMvc.perform(patch("/acortador/1/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.mensaje").value("La validacion fallo para uno o mas campos"))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }


    @Test
    void eliminarUrl_DebeRetornar200ConUrlEliminada() throws Exception {

        doNothing().when(urlServicio).EliminarUrl(1);
        mockMvc.perform(delete("/acortador/1/"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarUrl_CuandoNoExiste_DebeRetornar404() throws Exception {
        doThrow(new EntityNotFoundException("URL con id 99 no existe"))
                .when(urlServicio).EliminarUrl(99);

        mockMvc.perform(delete("/acortador/99/"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("URL_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

}