package fabio.dev.url_shortener.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarRespuesta(
        @Pattern(
                regexp = "^(http|https)://.*$",
                message = "URL inválida"
        )
        String url,
        @Size(max = 255, message = "El título no puede superar los 255 caracteres")
        String titulo,
        boolean cambiarSlug,
        boolean esClicked
) {
}
