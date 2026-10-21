package fabio.dev.url_shortener.RepositorioTests;

import fabio.dev.url_shortener.modelos.Url;
import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class UrlRepositorioTest {

    @Autowired
    private UrlRepositorio urlRepositorio;

    @Autowired
    private TestEntityManager em;

    private Url crearUrl(String slug, int clicks) {
        Url url = new Url();
        url.setSlug(slug);
        url.setOriginalUrl("https://ejemplo.com");
        url.setFechaRegistro(LocalDate.now().toString());
        url.setFechaModificacion(LocalDate.now().toString());
        url.setClicks(clicks);

        return em.persistAndFlush(url);
    }

    @Test
    void incrementar_Unclick(){
        Url url = crearUrl("asSlug", 0);

        int filasAfectadas = urlRepositorio.incrementarClicks("asSlug");

        em.clear();
        Url actualizada = urlRepositorio.findById(url.getId()).orElseThrow();

        assertEquals(1, filasAfectadas);
        assertEquals(1, actualizada.getClicks());

    }

    @Test
    void incrementar_variosClicks(){

        Url url = crearUrl("asSlug", 0);

        urlRepositorio.incrementarClicks("asSlug");
        urlRepositorio.incrementarClicks("asSlug");
        urlRepositorio.incrementarClicks("asSlug");

        em.clear();
        Url actualizada = urlRepositorio.findById(url.getId()).orElseThrow();

        assertEquals(3, actualizada.getClicks());

    }

    @Test
    void incrementar_deberiaFallar_PorSlugInexistente(){

        int filasAfectadas = urlRepositorio.incrementarClicks("emptySlug");
        assertEquals(0, filasAfectadas);
    }

    @Test
    void incrementar_NoDeberiAumentar_OtroUrl(){

        Url url = new Url();
        url.setSlug("clnSlug");
        url.setOriginalUrl("https://google.com");
        url.setFechaRegistro(LocalDate.now().toString());
        url.setFechaModificacion(LocalDate.now().toString());
        url.setClicks(0);

        em.persistAndFlush(url);

        crearUrl("domSlug",0);


        urlRepositorio.incrementarClicks("domSlug");
        em.clear();

        assertEquals(0, urlRepositorio.findById(url.getId()).orElseThrow().getClicks());

    }
}
