package fabio.dev.url_shortener.modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(unique = true)
    private String externalId;

    private String email;

    public Usuario(String externalId, String email) {
        this.externalId = externalId;
        this.email = email;
    }

}
