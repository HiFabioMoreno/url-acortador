package fabio.dev.url_shortener.servicios;

import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import org.springframework.stereotype.Component;

@Component
public class GeneradorTitulo {

    private static final String PREFIJO = "Enlace";

    private final UrlRepositorio urlRepositorio;

    public GeneradorTitulo(UrlRepositorio urlRepositorio) {
        this.urlRepositorio = urlRepositorio;
    }

    public String generar() {
        return PREFIJO + (urlRepositorio.count() + 1);
    }
}