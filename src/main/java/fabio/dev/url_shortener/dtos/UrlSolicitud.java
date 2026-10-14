package fabio.dev.url_shortener.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UrlSolicitud (
        @NotBlank
        @Pattern(
                regexp = "^(https)://.*$",
                message = "URL inválida"
        )
        String url
){
}