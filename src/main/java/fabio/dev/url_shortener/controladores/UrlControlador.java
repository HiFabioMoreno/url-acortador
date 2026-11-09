package fabio.dev.url_shortener.controladores;

import fabio.dev.url_shortener.dtos.ActualizarRespuesta;
import fabio.dev.url_shortener.dtos.UrlRespuesta;
import fabio.dev.url_shortener.dtos.UrlSolicitud;
import fabio.dev.url_shortener.servicios.UrlServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/acortador")
@Validated
public class UrlControlador {

    private UrlServicio urlServicio;

    public UrlControlador(UrlServicio urlServicio) {
        this.urlServicio = urlServicio;
    }

    @Operation(
            summary = "Obtener todas las URLs",
            description = "Obtiene una lista con todas las URLs acortadas registradas en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "URLs obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @GetMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ArrayList<UrlRespuesta>> ListarUrls(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(urlServicio.ListarUrls(jwt));
    }

    @Operation(
            summary = "Acortar una URL",
            description = "Crea una nueva URL acortada a partir de la URL original proporcionada en el cuerpo de la solicitud."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "URL acortada creada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos proporcionados no son válidos"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlRespuesta> registrarUrl(@Valid @RequestBody UrlSolicitud urlSolicitud, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(urlServicio.crearShortUrl(urlSolicitud, jwt));
    }

    @Operation(
            summary = "Actualizar una URL acortada",
            description = "Actualiza los datos de una URL acortada existente utilizando su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "URL actualizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos proporcionados no son válidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró la URL con el ID proporcionado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PatchMapping("/{id}/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlRespuesta> actualizarUrl(@PathVariable @Positive Integer id, @Valid @RequestBody ActualizarRespuesta dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(urlServicio.actualizarShortUrl(id, dto, jwt));
    }

    @Operation(
            summary = "Eliminar una URL acortada",
            description = "Elimina permanentemente una URL acortada existente utilizando su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "URL eliminada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró la URL con el ID proporcionado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @DeleteMapping("/{id}/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> eliminarUrl(@PathVariable @Positive Integer id, @AuthenticationPrincipal Jwt jwt) {
        urlServicio.EliminarUrl(id, jwt);
        return ResponseEntity.noContent().build();
    }

}