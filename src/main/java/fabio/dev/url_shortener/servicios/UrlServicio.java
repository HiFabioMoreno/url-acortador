package fabio.dev.url_shortener.servicios;

import fabio.dev.url_shortener.dtos.ActualizarRespuesta;
import fabio.dev.url_shortener.dtos.UrlRespuesta;
import fabio.dev.url_shortener.dtos.UrlSolicitud;
import fabio.dev.url_shortener.excepciones.InvalidInputException;
import fabio.dev.url_shortener.modelos.Url;
import fabio.dev.url_shortener.modelos.Usuario;
import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import jakarta.persistence.EntityNotFoundException;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

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
    public UrlRespuesta crearShortUrl(@NonNull UrlSolicitud urlSolicitud, String email) {

        logger.info("Creando slog para url: {}", urlSolicitud.url());

        Usuario usuario = usuarioServicio.comprobarUsuario(email);

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

    @Transactional(readOnly = true)
    public ArrayList<UrlRespuesta> ListarUrls(String email, Pageable pageable) {

        logger.info("Listando los urls del usuario autenticado");

        Usuario usuario = usuarioServicio.comprobarUsuario(email);

        Slice<Url> urls = urlRepositorio.findAllByUsuario_Id(usuario.getId(), pageable);

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
    public UrlRespuesta actualizarShortUrl(Integer id, ActualizarRespuesta actualizarRespuesta, String email)  {

        logger.info("Actualizando url con id : {}", id);

        if (actualizarRespuesta == null) {
            throw new InvalidInputException("La solicitud no puede estar vacia");
        }

        Usuario usuario = usuarioServicio.comprobarUsuario(email);

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
    public void EliminarUrl(Integer id, String email) {
        logger.info("Eliminando url con id: {}", id);

        Usuario usuario = usuarioServicio.comprobarUsuario(email);

        Url url = urlRepositorio.findByIdAndUsuario_Id(id, usuario.getId())
                .orElseThrow( () -> new EntityNotFoundException("El url no existe"));

        urlRepositorio.delete(url);

        logger.info("Url eliminada exitosamente");

    }

}
