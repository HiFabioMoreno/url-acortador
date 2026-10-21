package fabio.dev.url_shortener;

import fabio.dev.url_shortener.modelos.Url;
import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import fabio.dev.url_shortener.servicios.RedireccionUrlServicio;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedireccionUrlServicioTests {

    @Mock
    private UrlRepositorio urlRepositorio;

    @InjectMocks
    private RedireccionUrlServicio redireccionUrlServicio;

    private Url url;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);

        url = new Url();
        url.setId(1);
        url.setOriginalUrl("https://www.google.com");
        url.setSlug("uzdkr8");
        url.setFechaRegistro(LocalDateTime.now().toString());
        url.setFechaModificacion(LocalDateTime.now().toString());
        url.setClicks(0);
    }

    @Test
    @DisplayName("Deberia encontrar un url por su slug relacionado")
    public void deberiaEncontrar_ElUrlOginalDeUnSlug()  {

        when(urlRepositorio.findBySlug("uzdkr8")).thenReturn(Optional.of(url));

        String slug = redireccionUrlServicio.buscarUrlYRegistrarClick(url.getSlug());
        assertEquals("https://www.google.com", slug);

        verify(urlRepositorio, times(1)).findBySlug(anyString());
    }


    @Test
    @DisplayName("Deberia aumentar los clicks de un url ")
    public void deberiaAuemntar_LosClicksDeUnUrl()  {

        when(urlRepositorio.findBySlug("uzdkr8")).thenReturn(Optional.of(url));
        redireccionUrlServicio.buscarUrlYRegistrarClick(url.getSlug());
        verify(urlRepositorio, times(1)).incrementarClicks(url.getSlug());
    }

    @Test
    @DisplayName("Deberia dar error por slug inexistente")
    public void buscarAumentar_SlugInexistente_LanzarException(){

        when(urlRepositorio.findBySlug("noSlug")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, ()-> redireccionUrlServicio.buscarUrlYRegistrarClick("noSlug"));

    }

    @Test
    @DisplayName("No deberia de auemntar clicks por slug inexistente")
    public void buscarAumentar_SlugInexistente_NoAumentarClick(){

        when(urlRepositorio.findBySlug("noSlug")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, ()-> redireccionUrlServicio.buscarUrlYRegistrarClick("noSlug"));

        verify(urlRepositorio, never()).incrementarClicks(anyString());

    }

}
