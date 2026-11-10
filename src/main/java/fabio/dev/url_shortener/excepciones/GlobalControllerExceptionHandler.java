package fabio.dev.url_shortener.excepciones;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalControllerExceptionHandler {

    private final HttpServletRequest request;

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorRespuesta> handleInvalidInputException(InvalidInputException ex) {
            log.warn("Input invalido: {}",ex.getMessage());

            ErrorRespuesta error = ErrorRespuesta.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .errorCode(String.valueOf(ErrorCodes.URL_NO_VALIDA))
                    .mensaje(ex.getMessage())
                    .mensajeDesarrollador("Url no valida para procesar: " + ex.getMessage())
                    .path(request.getRequestId())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorRespuesta> HandlerEntityNotFoundException(EntityNotFoundException ex) {

        log.warn("URL no encontrada: {}", ex.getMessage());

        ErrorRespuesta error = ErrorRespuesta.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .errorCode(String.valueOf(ErrorCodes.URL_NO_ENCONTRADA))
                .mensaje(ex.getMessage())
                .mensajeDesarrollador("Url ID no existe en la base de datos "+ ex.getMessage())
                .path(request.getRequestId())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        ErrorRespuesta error = ErrorRespuesta.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errorCode(String.valueOf(ErrorCodes.URL_NO_VALIDA))
                .mensaje("Solicitud con cuerpo vacío o inválido")
                .mensajeDesarrollador("El cuerpo de la solicitud es inválido o está vacío")
                .path(request.getRequestId())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> HandlerMethodArgumentNotValidException( MethodArgumentNotValidException ex){
        log.warn("Error al validar parametros de datos: {}", ex.getMessage());

        List<ErrorRespuesta.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                    new ErrorRespuesta.FieldError(
                            error.getField(),
                            error.getRejectedValue(),
                            error.getDefaultMessage()
                    ))
                .collect(Collectors.toList());

        ErrorRespuesta error = ErrorRespuesta.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errorCode(String.valueOf(ErrorCodes.VALIDACION_FALLIDA))
                .mensaje("La validacion fallo para uno o mas campos")
                .mensajeDesarrollador("La validacion de la solicitud fallo")
                .path(request.getRequestId())
                .errors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorRespuesta> HandlerConstraintViolationException(ConstraintViolationException ex){
            List<ErrorRespuesta.ConstraintViolationError> errors =  ex.getConstraintViolations()
                    .stream()
                    .map(constraintViolation ->
                            new ErrorRespuesta.ConstraintViolationError(
                                    constraintViolation.getPropertyPath(),
                                    constraintViolation.getMessage()

                            ))
                    .collect(Collectors.toList());

            ErrorRespuesta error = ErrorRespuesta.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .errorCode(String.valueOf(ErrorCodes.VALIDACION_FALLIDA))
                    .mensaje("Algunos valores ingresados violan restricciones")
                    .mensajeDesarrollador("Violacion de restricciones, " + ex.getMessage())
                    .path(request.getRequestId())
                    .violacionesDeRestricciones(errors)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuesta> handleAccessDeniedException(AccessDeniedException ex) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean autenticado = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        HttpStatus status = autenticado ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
        ErrorCodes errorCode = autenticado ? ErrorCodes.ACCESO_DENEGADO : ErrorCodes.NO_AUTENTICADO;
        String mensaje = autenticado
                ? "No tiene permisos para acceder a este recurso"
                : "Se requiere autenticacion para acceder a este recurso";

        log.warn("Acceso denegado ({}): {}", status.value(), ex.getMessage());

        ErrorRespuesta error = ErrorRespuesta.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .errorCode(String.valueOf(errorCode))
                .mensaje(mensaje)
                .mensajeDesarrollador(ex.getClass().getSimpleName() + ": " + ex.getMessage())
                .path(request.getRequestId())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Violacion de integridad de datos: {}", ex.getMessage());

        ErrorRespuesta error = ErrorRespuesta.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .errorCode(String.valueOf(ErrorCodes.URL_DUPLICADA))
                .mensaje("El recurso ya existe o viola una restriccion de unicidad")
                .mensajeDesarrollador("Violacion de integridad de datos: " + ex.getMostSpecificCause().getMessage())
                .path(request.getRequestId())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> handleException(Exception ex){
        log.warn("A surgido un error inesperado ", ex);

        ErrorRespuesta error = ErrorRespuesta.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .errorCode(String.valueOf(ErrorCodes.INTERNAL_SERVER_ERROR))
                .mensaje("Un error inseprado a sucedido. Estamos trabajando para solucionarlo")
                .mensajeDesarrollador(ex.getClass().getSimpleName() + ": " + ex.getMessage())
                .path(request.getRequestId())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}