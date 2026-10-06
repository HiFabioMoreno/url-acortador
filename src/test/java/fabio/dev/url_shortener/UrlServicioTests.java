package fabio.dev.url_shortener;

import fabio.dev.url_shortener.dtos.ActualizarRespuesta;
import fabio.dev.url_shortener.dtos.UrlRespuesta;
import fabio.dev.url_shortener.dtos.UrlSolicitud;
import fabio.dev.url_shortener.excepciones.InvalidInputException;
import fabio.dev.url_shortener.modelos.Url;
import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import fabio.dev.url_shortener.servicios.GeneradorTitulo;
import fabio.dev.url_shortener.servicios.UrlServicio;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UrlServicioTests {

	@Mock
	private UrlRepositorio urlRepositorio;

	@Mock
	private GeneradorTitulo generadorTitulo;

	@InjectMocks
	private UrlServicio urlServicio;

	private Url url;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		url = new Url();
		url.setId(1);
		url.setOriginalUrl("https://www.google.com");
		url.setSlug("uzdkr8");
		url.setFechaRegistro(LocalDateTime.now().toString());
		url.setFechaModificacion(LocalDateTime.now().toString());
		url.setVecesAccedido(0);

	}

	@Test
	@DisplayName("Deberia guardar y acortar exitosamente un url")
	public void deberiaGuardarUrlExistosamente()  {

		when(generadorTitulo.generar()).thenReturn("Enlace1");
		when(urlRepositorio.save(any(Url.class))).thenAnswer(i -> {
				Url u = i.getArgument(0);
				u.setFechaRegistro(LocalDateTime.now().toString());
				return u;
		});

		UrlRespuesta nuevaUrl = urlServicio.crearShortUrl(new UrlSolicitud("https://www.google.com"));

		assertEquals("https://www.google.com",nuevaUrl.originalUrl());
		assertEquals("Enlace1",nuevaUrl.titulo());
		assertNotNull(nuevaUrl.fechaRegistro());
		assertNotNull(nuevaUrl.slug());
		assertEquals(0, nuevaUrl.vecesAccedido());

		verify(urlRepositorio, times(1)).save(any(Url.class));

	}

	@Test
	@DisplayName("Deberia encontrar un url por su slug relacionado")
	public void deberiaEncontrarElUrlOginalDeUnSlug()  {

		when(urlRepositorio.findBySlug("uzdkr8")).thenReturn(Optional.of(url));

		String slug = urlServicio.buscarUrl(url.getSlug());
		assertEquals("https://www.google.com", slug);

		verify(urlRepositorio, times(1)).findBySlug(anyString());
	}

	@Test
	@DisplayName("Deberia mostrar todos los urls guardados exitosamente")
	public void deberiaMostrarTodosLosUrlsGuardadosExitosamente(){

		List<Url> urlsMock = List.of(url);

		when(urlRepositorio.findAll()).thenReturn(urlsMock);

		ArrayList<UrlRespuesta> urls = urlServicio.ListarUrls();

		assertEquals(1, urls.size());
		assertEquals("https://www.google.com", urls.get(0).originalUrl());

		verify(urlRepositorio, times(1)).findAll();

	}

	@Test
	@DisplayName("Deberia actualizar el contador de un url exitosamente")
	public void deberiaActualizarElContadorDelUrlExistosamente() {

		when(urlRepositorio.findById(anyInt())).thenReturn(Optional.of(this.url));
		when(urlRepositorio.save(any(Url.class))).thenReturn(this.url);

		UrlRespuesta urlRespuesta = urlServicio.actualizarShortUrl(
				1, new ActualizarRespuesta(null, null, false, true)
		);

		assertEquals(1,urlRespuesta.vecesAccedido());
		assertEquals("https://www.google.com",urlRespuesta.originalUrl());

		verify(urlRepositorio, times(1)).findById(anyInt());

	}

	@Test
	@DisplayName("Deberia actualizar el url exitosamente")
	public void deberiaActualizarElUrlExistosamente() {

		when(urlRepositorio.findById(anyInt())).thenReturn(Optional.of(this.url));
		when(urlRepositorio.save(any(Url.class))).thenReturn(this.url);

		UrlRespuesta urlRespuesta = urlServicio.actualizarShortUrl(
				1, new ActualizarRespuesta("https://www.youtube.com/", null, false, false)
		);

		assertEquals(0,urlRespuesta.vecesAccedido());
		assertEquals("https://www.youtube.com/",urlRespuesta.originalUrl());

		verify(urlRepositorio, times(1)).findById(anyInt());

	}

	@Test
	@DisplayName("Deberia actualizar el titulo exitosamente")
	public void deberiaActualizarElTituloExistosamente() {

		when(urlRepositorio.findById(anyInt())).thenReturn(Optional.of(this.url));
		when(urlRepositorio.save(any(Url.class))).thenReturn(this.url);

		UrlRespuesta urlRespuesta = urlServicio.actualizarShortUrl(
				1, new ActualizarRespuesta(null, "Google Home", false, false)
		);

		assertEquals("Google Home", urlRespuesta.titulo());
		assertEquals("https://www.google.com", urlRespuesta.originalUrl());

		verify(urlRepositorio, times(1)).findById(anyInt());

	}

	@Test
	@DisplayName("Deberia actualizar el slug exitosamente")
	public void deberiaActualizarSlugExistosamente() {

		when(urlRepositorio.findById(anyInt())).thenReturn(Optional.of(this.url));
		when(urlRepositorio.save(any(Url.class))).thenReturn(this.url);

		UrlRespuesta urlRespuesta = urlServicio.actualizarShortUrl(
				1, new ActualizarRespuesta(null, null, true, false)
		);

		assertEquals(0,urlRespuesta.vecesAccedido());
		assertEquals("https://www.google.com",urlRespuesta.originalUrl());
		assertNotEquals("uzdkr8", urlRespuesta.slug());

		verify(urlRepositorio, times(1)).findById(anyInt());

	}

	@Test
	@DisplayName("Deberia dar InvalidInputException por dto null al actualizar")
	public void deberiaDarRuntimeExceptionPorDtoNullActualizar() {

		InvalidInputException err = assertThrows(InvalidInputException.class, () -> urlServicio.actualizarShortUrl(1, null));

		assertEquals("La solicitud no puede estar vacia", err.getMessage());
		verify(urlRepositorio, never()).findById(anyInt());
		verify(urlRepositorio, never()).save(any(Url.class));

	}

	@Test
	@DisplayName("Deberia dar InvalidInputException por id null al actualizar")
	public void deberiaDarRuntimeExceptionActualizar() {

		RuntimeException err = assertThrows(InvalidInputException.class, () -> urlServicio.actualizarShortUrl(null, null));

		assertEquals("La solicitud no puede estar vacia", err.getMessage());

		verify(urlRepositorio, never()).findById(anyInt());
		verify(urlRepositorio, never()).save(any(Url.class));

	}

	@Test
	@DisplayName("Deberia dar InvalidInputException por id negativo al actualizar")
	public void deberiaDarRuntimeExceptionPorIdNegativoAlActualizar() {

		InvalidInputException err = assertThrows(InvalidInputException.class, () -> urlServicio.actualizarShortUrl(-1, null));

		assertEquals("La solicitud no puede estar vacia", err.getMessage());

		verify(urlRepositorio, never()).findById(anyInt());
		verify(urlRepositorio, never()).save(any(Url.class));

	}

	@Test
	@DisplayName("Deberia eliminar exitosamente un url")
	public void deberiaEliminarUnUrlExitosamente() {
		when(urlRepositorio.existsById(anyInt())).thenReturn(true);

		urlServicio.EliminarUrl(1);

		verify(urlRepositorio, times(1)).deleteById(anyInt());

	}


	@Test
	@DisplayName("Deberia dar EntityNotFoundException por url no existente al eliminar")
	public void deberiaFallarAlEliminarUnUrlQueNoExiste() {

		EntityNotFoundException err = assertThrows(
				EntityNotFoundException.class, () -> urlServicio.EliminarUrl(1000)
		);

		assertEquals("El url no existe", err.getMessage());


		verify(urlRepositorio, times(1)).existsById(anyInt());
		verify(urlRepositorio, never()).deleteById(anyInt());
	}

}