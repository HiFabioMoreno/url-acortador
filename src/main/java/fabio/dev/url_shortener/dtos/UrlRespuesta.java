package fabio.dev.url_shortener.dtos;

import java.time.LocalDate;

public record UrlRespuesta(
        Integer idUrl,
        String originalUrl,
        String titulo,
        String slug,
        LocalDate fechaRegistro,
        LocalDate fechaModificacion,
        Long vecesAccedido
) {
}
