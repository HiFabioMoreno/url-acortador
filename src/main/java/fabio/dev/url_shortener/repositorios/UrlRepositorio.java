package fabio.dev.url_shortener.repositorios;

import fabio.dev.url_shortener.modelos.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UrlRepositorio extends JpaRepository<Url,Integer> {
    Optional<Url> findBySlug(String slug);

    @Modifying
    @Query("UPDATE Url u SET u.clicks = u.clicks + 1 WHERE u.slug = ?1")
    int incrementarClicks(@Param("slug")String slug);

}
