package fabio.dev.url_shortener;

import fabio.dev.url_shortener.dtos.ActualizarRespuesta;
import fabio.dev.url_shortener.dtos.UrlRespuesta;
import fabio.dev.url_shortener.dtos.UrlSolicitud;
import fabio.dev.url_shortener.excepciones.InvalidInputException;
import fabio.dev.url_shortener.modelos.Url;
import fabio.dev.url_shortener.modelos.Usuario;
import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import fabio.dev.url_shortener.servicios.GeneradorTitulo;
import fabio.dev.url_shortener.servicios.UrlServicio;
import fabio.dev.url_shortener.servicios.UsuarioServicio;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServicioTests {

	@Mock
	private UrlRepositorio urlRepositorio;

	@Mock
	private GeneradorTitulo generadorTitulo;

	@Mock
	private UsuarioServicio usuarioServicio;

	@InjectMocks
	private UrlServicio urlServicio;

	private Url url;

	private Usuario usuario;

	private Jwt jwt;

	@BeforeEach
	void setUp() {

		usuario = new Usuario("user-123", "user@test.com");
		usuario.setId(10L);

		jwt = Jwt.withTokenValue("token")
				.header("alg", "none")
				.subject("user-123")
				.claim("email", "user@test.com")
				.build();

		url = new Url();
		url.setId(1);
		url.setOriginalUrl("https://www.google.com");
		url.setSlug("uzdkr8");
		url.setUsuario(usuario);
		url.setFechaRegistro(LocalDate.now());
		url.setFechaModificacion(LocalDate.now());
		url.setClicks(0L);

	}

	@Test
	@DisplayName("Deberia guardar y acortar exitosamente un url asignado al usuario autenticado")
	public void deberiaGuardarUrlExistosamente()  {

		when(usuarioServicio.obtenerOCrear(jwt)).thenReturn(usuario);
		when(generadorTitulo.generar()).thenReturn("Enlace1");
		when(urlRepositorio.save(any(Url.class))).thenAnswer(i -> {
				Url u = i.getArgument(0);
				u.setId(1);
				return u;
		});

		UrlRespuesta nuevaUrl = urlServicio.crearShortUrl(new UrlSolicitud("https://www.google.com"), jwt);

		assertEquals("https://www.google.com",nuevaUrl.originalUrl());
		assertEquals("Enlace1",nuevaUrl.titulo());
		assertNotNull(nuevaUrl.fechaRegistro());
		assertNotNull(nuevaUrl.fechaModificacion());
		assertNotNull(nuevaUrl.slug());
		assertEquals(0L, nuevaUrl.vecesAccedido());

		verify(urlRepositorio, times(1)).save(argThat(saved -> saved.getUsuario().equals(usuario)));

	}

	@Test
	@DisplayName("Deberia listar solo los urls del usuario autenticado")
	public void deberiaMostrarTodosLosUrlsGuardadosExitosamente(){

		List<Url> urlsMock = List.of(url);

		when(usuarioServicio.obtenerOCrear(jwt)).thenReturn(usuario);
		when(urlRepositorio.findAllByUsuario_Id(usuario.getId())).thenReturn(urlsMock);

		ArrayList<UrlRespuesta> urls = urlServicio.ListarUrls(jwt);

		assertEquals(1, urls.size());
		assertEquals("https://www.google.com", urls.get(0).originalUrl());

		verify(urlRepositorio, times(1)).findAllByUsuario_Id(usuario.getId());
		verify(urlRepositorio, never()).findAll();

	}

	@Test
	@DisplayName("Deberia actualizar el titulo exitosamente")
	public void deberiaActualizarElTituloExistosamente() {

		when(usuarioServicio.obtenerOCrear(jwt)).thenReturn(usuario);
		when(urlRepositorio.findByIdAndUsuario_Id(1, usuario.getId())).thenReturn(Optional.of(this.url));

		UrlRespuesta urlRespuesta = urlServicio.actualizarShortUrl(
				1, new ActualizarRespuesta( "Google Home"), jwt
		);

		assertEquals("Google Home", urlRespuesta.titulo());
		assertEquals("https://www.google.com", urlRespuesta.originalUrl());

		verify(urlRepositorio, times(1)).findByIdAndUsuario_Id(1, usuario.getId());

	}

	@Test
	@DisplayName("Deberia dar InvalidInputException por dto null al actualizar")
	public void deberiaDarInvalidInputExceptionPorDtoNullActualizar() {

		InvalidInputException err = assertThrows(InvalidInputException.class,
				() -> urlServicio.actualizarShortUrl(1, null, jwt));

		assertEquals("La solicitud no puede estar vacia", err.getMessage());
		verify(urlRepositorio, never()).findByIdAndUsuario_Id(anyInt(), anyLong());
		verify(urlRepositorio, never()).save(any(Url.class));

	}

	@Test
	@DisplayName("Deberia dar EntityNotFoundException al actualizar un url inexistente")
	public void deberiaDarEntityNotFoundExceptionAlActualizarUrlInexistente() {

		when(usuarioServicio.obtenerOCrear(jwt)).thenReturn(usuario);
		when(urlRepositorio.findByIdAndUsuario_Id(999, usuario.getId())).thenReturn(Optional.empty());

		EntityNotFoundException err = assertThrows(EntityNotFoundException.class,
				() -> urlServicio.actualizarShortUrl(999, new ActualizarRespuesta("Titulo"), jwt));

		assertEquals("El url no existe", err.getMessage());

		verify(urlRepositorio, never()).save(any(Url.class));

	}

	@Test
	@DisplayName("Deberia dar EntityNotFoundException al actualizar un url de otro usuario")
	public void deberiaDarEntityNotFoundExceptionAlActualizarUrlDeOtroUsuario() {

		when(usuarioServicio.obtenerOCrear(jwt)).thenReturn(usuario);
		when(urlRepositorio.findByIdAndUsuario_Id(1, usuario.getId())).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class,
				() -> urlServicio.actualizarShortUrl(1, new ActualizarRespuesta("Titulo"), jwt));

		verify(urlRepositorio, never()).save(any(Url.class));

	}

	@Test
	@DisplayName("Deberia eliminar exitosamente un url del usuario autenticado")
	public void deberiaEliminarUnUrlExitosamente() {

		when(usuarioServicio.obtenerOCrear(jwt)).thenReturn(usuario);
		when(urlRepositorio.findByIdAndUsuario_Id(1, usuario.getId())).thenReturn(Optional.of(url));

		urlServicio.EliminarUrl(1, jwt);

		verify(urlRepositorio, times(1)).delete(url);

	}

	@Test
	@DisplayName("Deberia dar EntityNotFoundException por url no existente al eliminar")
	public void deberiaFallarAlEliminarUnUrlQueNoExiste() {

		when(usuarioServicio.obtenerOCrear(jwt)).thenReturn(usuario);
		when(urlRepositorio.findByIdAndUsuario_Id(1000, usuario.getId())).thenReturn(Optional.empty());

		EntityNotFoundException err = assertThrows(
				EntityNotFoundException.class, () -> urlServicio.EliminarUrl(1000, jwt)
		);

		assertEquals("El url no existe", err.getMessage());

		verify(urlRepositorio, times(1)).findByIdAndUsuario_Id(1000, usuario.getId());
		verify(urlRepositorio, never()).delete(any(Url.class));
	}

	@Test
	@DisplayName("Deberia dar EntityNotFoundException al eliminar un url de otro usuario")
	public void deberiaFallarAlEliminarUnUrlDeOtroUsuario() {

		when(usuarioServicio.obtenerOCrear(jwt)).thenReturn(usuario);
		when(urlRepositorio.findByIdAndUsuario_Id(1, usuario.getId())).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> urlServicio.EliminarUrl(1, jwt));

		verify(urlRepositorio, never()).delete(any(Url.class));
	}

}
