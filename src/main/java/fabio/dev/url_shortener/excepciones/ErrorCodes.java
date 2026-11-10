package fabio.dev.url_shortener.excepciones;

public enum ErrorCodes {

    // ERRORES EN URLS
    URL_NO_VALIDA,
    URL_NO_ENCONTRADA,
    URL_DUPLICADA,
    VALIDACION_FALLIDA,

    // ERRORES DE SEGURIDAD
    NO_AUTENTICADO,
    ACCESO_DENEGADO,

    // ERRORES INTERNOS
    INTERNAL_SERVER_ERROR
}