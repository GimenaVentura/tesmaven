package pe.edu.vallegrande.workshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.workshop.model.Workshop;
import pe.edu.vallegrande.workshop.service.WorkshopService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/api/workshops")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;

    @GetMapping("/list")
    public Flux<Workshop> getWorkshop() {
        return workshopService.findAllWorkshop()
                .doOnNext(workshop -> log.info("Workshop data: {}", workshop));
    }

    @GetMapping("/{id}")
    public Mono<Workshop> getWorkshopById(@PathVariable Long id) {
        return workshopService.findById(id);
    }

    @GetMapping("/active")
    public Flux<Workshop> getWorkshopActivos() {
        return workshopService.getActivosByState("A");
    }

    @GetMapping("/inactive")
    public Flux<Workshop> getWorkshopInactivos() {
        return workshopService.findStatus("I");
    }

    @PostMapping("/create")
    public Mono<Workshop> createWorkshop(@RequestBody Workshop workshop) {
        return workshopService.createWorkshop(workshop);
    }

    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<Workshop>> updateWorkshop(@PathVariable Long id, @RequestBody Workshop updatedWorkshop) {
        return workshopService.findById(id)
                .flatMap(existingWorkshop -> {
                    existingWorkshop.setName(updatedWorkshop.getName());
                    existingWorkshop.setDescription(updatedWorkshop.getDescription());
                    existingWorkshop.setStartDate(updatedWorkshop.getStartDate());
                    existingWorkshop.setEndDate(updatedWorkshop.getEndDate());
                    existingWorkshop.setObservation(updatedWorkshop.getObservation());
                    existingWorkshop.setPersonId(updatedWorkshop.getPersonId());
                    if (updatedWorkshop.getState() != null) {
                        existingWorkshop.setState(updatedWorkshop.getState());
                    }
                    return workshopService.updateWorkshop(existingWorkshop); // 👈 importante
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/activate/{id}")
    public Mono<ResponseEntity<Workshop>> activateWorkshop(@PathVariable Long id) {
        return workshopService.restoreWorkshop(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<Void>> deleteWorkshop(@PathVariable Long id) {
        return workshopService.deleteById(id)
                .thenReturn(ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/deactive/{id}")
    public Mono<ResponseEntity<Void>> deactivateWorkshop(@PathVariable Long id) {
        return workshopService.logicalDelete(id)
                .thenReturn(ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
