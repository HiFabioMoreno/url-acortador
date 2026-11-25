package fabio.dev.url_shortener.controladores;

import fabio.dev.url_shortener.dtos.UsuarioRespuesta;
import fabio.dev.url_shortener.servicios.UsuarioServicio;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuarioControlador {

    private UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @PostMapping("/registro/")
    public UsuarioRespuesta crearUsuario(String email, String password) {
        return this.usuarioServicio.registrarUsuario(email, password);
    }

}
