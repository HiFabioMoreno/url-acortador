package fabio.dev.url_shortener.RepositorioTests;

import fabio.dev.url_shortener.modelos.Url;
import fabio.dev.url_shortener.modelos.Usuario;
import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import fabio.dev.url_shortener.repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UrlRepositorioTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UrlRepositorio urlRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private TestEntityManager em;

    private Usuario crearUsuario() {
        return usuarioRepositorio.findByExternalId("user-123")
                .orElseGet(() -> em.persistAndFlush(new Usuario("user-123", "user-123@test.com")));
    }

    private Url crearUrl(String slug, long clicks) {
        Url url = new Url();
        url.setSlug(slug);
        url.setOriginalUrl("https://ejemplo.com");
        url.setUsuario(crearUsuario());
        url.setFechaRegistro(LocalDate.now());
        url.setFechaModificacion(LocalDate.now());
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
        assertEquals(1L, actualizada.getClicks());

    }

    @Test
    void incrementar_variosClicks(){

        Url url = crearUrl("asSlug", 0);

        urlRepositorio.incrementarClicks("asSlug");
        urlRepositorio.incrementarClicks("asSlug");
        urlRepositorio.incrementarClicks("asSlug");

        em.clear();
        Url actualizada = urlRepositorio.findById(url.getId()).orElseThrow();

        assertEquals(3L, actualizada.getClicks());

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
        url.setUsuario(crearUsuario());
        url.setFechaRegistro(LocalDate.now());
        url.setFechaModificacion(LocalDate.now());
        url.setClicks(0L);

        em.persistAndFlush(url);

        crearUrl("domSlug",0);


        urlRepositorio.incrementarClicks("domSlug");
        em.clear();

        assertEquals(0L, urlRepositorio.findById(url.getId()).orElseThrow().getClicks());

    }
}
