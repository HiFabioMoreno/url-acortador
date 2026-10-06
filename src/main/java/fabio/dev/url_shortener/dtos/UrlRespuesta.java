package fabio.dev.url_shortener.dtos;

public record UrlRespuesta(
        Integer idUrl,
        String originalUrl,
        String titulo,
        String slug,
        String fechaRegistro,
        String fechaModificacion,
        Integer vecesAccedido
) {
}
