package pe.edu.vallegrande.workshop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import pe.edu.vallegrande.workshop.config.DotenvInitializer;
import pe.edu.vallegrande.workshop.config.TestSecurityConfig;
import pe.edu.vallegrande.workshop.model.Workshop;
import pe.edu.vallegrande.workshop.service.WorkshopService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@ActiveProfiles("test")
@WebFluxTest(controllers = WorkshopController.class)
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)  // Clase para desactivar seguridad en tests
@ContextConfiguration(initializers = DotenvInitializer.class)  // Solo si usas initializer
class WorkshopControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WorkshopService workshopService;  // MockBean para el servicio

    private Workshop workshop;

    @BeforeEach
    void setUp() {
        workshop = new Workshop();
        workshop.setId(1L);
        workshop.setName("Curso de Java");
        workshop.setDescription("Aprende Java desde cero");
        workshop.setStartDate(LocalDate.of(2025, 5, 1));
        workshop.setEndDate(LocalDate.of(2025, 5, 31));
        workshop.setObservation("Incluye proyectos");
        workshop.setState("A");
        workshop.setPersonId("2,10,11,19,18,17,25,24");
    }

    @Test
    void testGetWorkshopList() {
        Mockito.when(workshopService.findAllWorkshop()).thenReturn(Flux.just(workshop));

        webTestClient.get()
                .uri("/api/workshops/list")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Workshop.class)
                .hasSize(1)
                .value(workshops -> {
                    assert workshops.get(0).getName().equals("Curso de Java");
                });
    }

    @Test
    void testGetWorkshopById() {
        Mockito.when(workshopService.findById(1L)).thenReturn(Mono.just(workshop));

        webTestClient.get()
                .uri("/api/workshops/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Curso de Java");
    }

    @Test
    void testCreateWorkshop() {
        Mockito.when(workshopService.createWorkshop(Mockito.any())).thenReturn(Mono.just(workshop));

        webTestClient.post()
                .uri("/api/workshops/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(workshop)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Curso de Java");
    }

    @Test
    void testDeleteWorkshop() {
        Mockito.when(workshopService.deleteById(1L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/workshops/delete/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
void testGetActiveWorkshops() {
    Mockito.when(workshopService.getActivosByState("A")).thenReturn(Flux.just(workshop));

    webTestClient.get()
            .uri("/api/workshops/active")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Workshop.class)
            .hasSize(1)
            .value(workshops -> {
                assert workshops.get(0).getState().equals("A");
            });
}

@Test
void testGetInactiveWorkshops() {
    workshop.setState("I");
    Mockito.when(workshopService.findStatus("I")).thenReturn(Flux.just(workshop));

    webTestClient.get()
            .uri("/api/workshops/inactive")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Workshop.class)
            .hasSize(1)
            .value(workshops -> {
                assert workshops.get(0).getState().equals("I");
            });
}

@Test
void testUpdateWorkshop_Success() {
    Workshop updatedWorkshop = new Workshop();
    updatedWorkshop.setName("Curso Avanzado");
    updatedWorkshop.setDescription("Aprende Java avanzado");
    updatedWorkshop.setStartDate(LocalDate.of(2025, 6, 1));
    updatedWorkshop.setEndDate(LocalDate.of(2025, 6, 30));
    updatedWorkshop.setObservation("Incluye proyectos avanzados");
    updatedWorkshop.setPersonId("3,5,7");
    updatedWorkshop.setState("A");

    Mockito.when(workshopService.findById(1L)).thenReturn(Mono.just(workshop));
    Mockito.when(workshopService.updateWorkshop(Mockito.any(Workshop.class))).thenReturn(Mono.just(updatedWorkshop));

    webTestClient.put()
            .uri("/api/workshops/update/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedWorkshop)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Curso Avanzado")
            .jsonPath("$.description").isEqualTo("Aprende Java avanzado");
}

@Test
void testUpdateWorkshop_NotFound() {
    Mockito.when(workshopService.findById(1L)).thenReturn(Mono.empty());

    webTestClient.put()
            .uri("/api/workshops/update/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(workshop)
            .exchange()
            .expectStatus().isNotFound();
}

@Test
void testActivateWorkshop_Success() {
    Mockito.when(workshopService.restoreWorkshop(1L)).thenReturn(Mono.just(workshop));

    webTestClient.put()
            .uri("/api/workshops/activate/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(1);
}

@Test
void testActivateWorkshop_NotFound() {
    Mockito.when(workshopService.restoreWorkshop(1L)).thenReturn(Mono.empty());

    webTestClient.put()
            .uri("/api/workshops/activate/1")
            .exchange()
            .expectStatus().isNotFound();
}

@Test
void testDeactivateWorkshop_Success() {
    Mockito.when(workshopService.logicalDelete(1L)).thenReturn(Mono.empty());

    webTestClient.delete()
            .uri("/api/workshops/deactive/1")
            .exchange()
            .expectStatus().isOk();
}

@Test
void testDeactivateWorkshop_NotFound() {
    Mockito.when(workshopService.logicalDelete(1L)).thenReturn(Mono.error(new RuntimeException("Not found")));

    webTestClient.delete()
            .uri("/api/workshops/deactive/1")
            .exchange()
            .expectStatus().is5xxServerError();
}

}
