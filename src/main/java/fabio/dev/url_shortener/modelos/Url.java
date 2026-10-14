package fabio.dev.url_shortener.modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Data
@Entity
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El url no puede estar vacio")
    @Column(unique = true, length = 2048)
    private String originalUrl;

    @NotBlank(message = "El slug no puede estar vacio")
    @Column(unique = true)
    private String slug;

    @Column(name = "titulo", length = 255)
    private String titulo;

    @Column(name = "fechaRegistro", nullable = false, updatable = false)
    private String fechaRegistro;

    @Column(name = "fechaModificacion", nullable = false)
    private String fechaModificacion;

    @PositiveOrZero(message = "El contador de accesos no puede ser negativo")
    private Integer vecesAccedido;

    @PrePersist
    void onCreate() {
        this.fechaRegistro = GenerarTimestamp();
    }

    @PreUpdate
    void onUpdate() {
        this.fechaModificacion = GenerarTimestamp();
    }

    public static String GenerarTimestamp(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String fechaFormateada = LocalDateTime.now().format(formatter);

        return fechaFormateada;
    }

    public static String GeneradorUrl(){

        StringBuilder sb = new StringBuilder();

        String chars = "abcdefghijklmopqrstuvwxyz0123456789";
        ArrayList<String> array = new ArrayList<>();

        for(String str : chars.split("")){
            array.add(str);
        }

        Collections.shuffle(array);

        for(int i = 0; i <= 5; i++){
            sb.append(array.get(i).toString());
        }

        return sb.toString();
    }

}
