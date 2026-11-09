package fabio.dev.url_shortener.servicios;

import fabio.dev.url_shortener.dtos.ActualizarRespuesta;
import fabio.dev.url_shortener.dtos.UrlRespuesta;
import fabio.dev.url_shortener.dtos.UrlSolicitud;
import fabio.dev.url_shortener.excepciones.InvalidInputException;
import fabio.dev.url_shortener.modelos.Url;
import fabio.dev.url_shortener.modelos.Usuario;
import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static fabio.dev.url_shortener.modelos.Url.GeneradorUrl;
import static fabio.dev.url_shortener.modelos.Url.GenerarTimestamp;

@Service
public class UrlServicio {

    private UrlRepositorio urlRepositorio;
    private final GeneradorTitulo generadorTitulo;
    private final Logger logger = LoggerFactory.getLogger(UrlServicio.class);

    private UsuarioServicio usuarioServicio;

    public UrlServicio(UrlRepositorio urlRepositorio, GeneradorTitulo generadorTitulo,  UsuarioServicio usuarioServicio) {
        this.urlRepositorio = urlRepositorio;
        this.generadorTitulo = generadorTitulo;
        this.usuarioServicio = usuarioServicio;
    }

    @Transactional
    public UrlRespuesta crearShortUrl(@NonNull UrlSolicitud urlSolicitud, Jwt jwt) {

        logger.info("Creando slog para url: {}", urlSolicitud.url());

        Usuario usuario = usuarioServicio.obtenerOCrear(jwt);

        Url shortUrl = new Url();
        shortUrl.setOriginalUrl(urlSolicitud.url());
        shortUrl.setTitulo(generadorTitulo.generar());
        shortUrl.setSlug(GeneradorUrl());
        shortUrl.setFechaRegistro(GenerarTimestamp());
        shortUrl.setFechaModificacion(GenerarTimestamp());
        shortUrl.setClicks(0L);
        shortUrl.setUsuario(usuario);

        urlRepositorio.save(shortUrl);

        logger.info("Url procesada y guardada exitosamente");

        return new UrlRespuesta(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getTitulo(),
                shortUrl.getSlug(),
                shortUrl.getFechaRegistro(),
                shortUrl.getFechaModificacion(),
                shortUrl.getClicks());
    }

    public ArrayList<UrlRespuesta> ListarUrls(Jwt jwt) {

        logger.info("Listando los urls del usuario autenticado");

        Usuario usuario = usuarioServicio.obtenerOCrear(jwt);

        List<Url> urls = urlRepositorio.findAllByUsuario_Id(usuario.getId());

        ArrayList<UrlRespuesta> urlsList = new ArrayList<>();

        urls.forEach(url -> {
            urlsList.add(new UrlRespuesta(
                    url.getId(),
                    url.getOriginalUrl(),
                    url.getTitulo(),
                    url.getSlug(),
                    url.getFechaRegistro(),
                    url.getFechaModificacion(),
                    url.getClicks()));
        });

        return urlsList;
    }

    @Transactional
    public UrlRespuesta actualizarShortUrl(Integer id, ActualizarRespuesta actualizarRespuesta, Jwt jwt)  {

        logger.info("Actualizando url con id : {}", id);

        if (actualizarRespuesta == null) {
            throw new InvalidInputException("La solicitud no puede estar vacia");
        }

        Usuario usuario = usuarioServicio.obtenerOCrear(jwt);

        Url url = urlRepositorio.findByIdAndUsuario_Id(id, usuario.getId())
                .orElseThrow( () -> new EntityNotFoundException("El url no existe"));

        if (actualizarRespuesta.titulo() != null){
            url.setTitulo(actualizarRespuesta.titulo());
        }

        logger.info("Url actualizada exitosamente");

        return new UrlRespuesta(
                url.getId(),
                url.getOriginalUrl(),
                url.getTitulo(),
                url.getSlug(),
                url.getFechaRegistro(),
                url.getFechaModificacion(),
                url.getClicks());

    }

    @Transactional
    public void EliminarUrl(Integer id, Jwt jwt){
        logger.info("Eliminando url con id: {}", id);

        Usuario usuario = usuarioServicio.obtenerOCrear(jwt);

        Url url = urlRepositorio.findByIdAndUsuario_Id(id, usuario.getId())
                .orElseThrow( () -> new EntityNotFoundException("El url no existe"));

        urlRepositorio.delete(url);

        logger.info("Url eliminada exitosamente");

    }

}
