package fabio.dev.url_shortener;

import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import fabio.dev.url_shortener.servicios.GeneradorTitulo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class GeneradorTituloTests {

    @Mock
    private UrlRepositorio urlRepositorio;

    @InjectMocks
    private GeneradorTitulo generadorTitulo;

    @Test
    @DisplayName("Deberia generar Enlace1 cuando no hay urls guardadas")
    void deberiaGenerarEnlace1SinUrlsGuardadas() {

        MockitoAnnotations.openMocks(this);
        when(urlRepositorio.count()).thenReturn(0L);

        assertEquals("Enlace1", generadorTitulo.generar());
    }

    @Test
    @DisplayName("Deberia generar el titulo segun la cantidad de urls guardadas")
    void deberiaGenerarTituloSegunCantidadDeUrlsGuardadas() {

        MockitoAnnotations.openMocks(this);
        when(urlRepositorio.count()).thenReturn(5L);

        assertEquals("Enlace6", generadorTitulo.generar());
    }
}