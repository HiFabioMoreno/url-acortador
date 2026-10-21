package fabio.dev.url_shortener.servicios;

import fabio.dev.url_shortener.modelos.Url;
import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RedireccionUrlServicio {

    private UrlRepositorio urlRepositorio;

    private final Logger logger = LoggerFactory.getLogger(UrlServicio.class);

    public RedireccionUrlServicio( UrlRepositorio urlRepositorio ){
        this.urlRepositorio = urlRepositorio;
    }

    @Transactional
    public String buscarUrlYRegistrarClick(String slug) {
        logger.info("Buscando slug: {}", slug);

        Url url = urlRepositorio.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("No existe la URL: " + slug));

        int filas = urlRepositorio.incrementarClicks(slug);

        logger.info("slug encontrado y click incrementado existosamente");

        return url.getOriginalUrl();
    }
}
