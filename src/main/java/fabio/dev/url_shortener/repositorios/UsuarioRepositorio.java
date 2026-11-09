package fabio.dev.url_shortener.repositorios;

import fabio.dev.url_shortener.modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepositorio  extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByExternalId(String id);

}
