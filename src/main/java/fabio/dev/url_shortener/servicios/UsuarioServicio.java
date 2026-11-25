package fabio.dev.url_shortener.servicios;

import fabio.dev.url_shortener.dtos.UsuarioRespuesta;
import fabio.dev.url_shortener.modelos.Usuario;
import fabio.dev.url_shortener.repositorios.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServicio {

    private Logger log = LoggerFactory.getLogger(UsuarioServicio.class);
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioRespuesta registrarUsuario(String email, String password) {

        log.info("Iniciando registro de usuario");

        if (usuarioRepositorio.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRole("ROLE_USER");
        usuario.setEnabled(true);

        Usuario guardado = usuarioRepositorio.save(usuario);

        log.info("Usuario registrado com sucesso");

        return new UsuarioRespuesta(
                guardado.getId(),
                guardado.getEmail(),
                guardado.getRole(),
                guardado.getEnabled()
        );
    }

    public Usuario comprobarUsuario(String email) {
        log.info("Iniciando comprobar de usuario");

        return usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

}
