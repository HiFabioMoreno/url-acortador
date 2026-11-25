package fabio.dev.url_shortener.dtos;

public record UsuarioRespuesta(
        Long id,
        String email,
        String role,
        Boolean enabled
) {
}
