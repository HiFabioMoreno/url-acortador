package fabio.dev.url_shortener.servicios;

import fabio.dev.url_shortener.modelos.Usuario;
import fabio.dev.url_shortener.repositorios.UsuarioRepositorio;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServicio {

    private UsuarioRepositorio usuarioRepositorio;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Usuario obtenerOCrear(Jwt jwt) {
        return usuarioRepositorio.findByExternalId(jwt.getSubject())
                .orElseGet(() -> usuarioRepositorio.save(
                        new Usuario(jwt.getSubject(), jwt.getClaimAsString("email"))));
    }

}