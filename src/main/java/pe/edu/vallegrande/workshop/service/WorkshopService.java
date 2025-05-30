package pe.edu.vallegrande.workshop.service;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.workshop.dto.WorkshopKafkaEventDto;
import pe.edu.vallegrande.workshop.model.Workshop;
import pe.edu.vallegrande.workshop.repository.WorkshopRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkshopService {
    private final WorkshopRepository workshopRepository;
    private final KafkaProducerService kafkaProducerService;

    public Flux<Workshop> findAllWorkshop() {
        return workshopRepository.findAll();  
    }
    
    public Flux<Workshop> findStatus(String state){
        return workshopRepository.findAllByState(state);
    }

    public Flux<Workshop> getActivosByState(String state){
        return workshopRepository.findAllByState(state);
    }

    public Mono<Void> inactiveWorkshop(Long id) {
        return workshopRepository.inactiveWorkshop(id);
    }

    public Mono<Workshop> findById(Long id) {
        return workshopRepository.findById(id);
    }

    public Mono<Workshop> createWorkshop(Workshop workshop) {
        if (workshop.getState() == null || workshop.getState().isBlank()) {
            workshop.setState("A"); // ✅ Valor por defecto
        }
        return workshopRepository.save(workshop)
                .doOnNext(saved -> kafkaProducerService.sendWorkshopEvent(toEventDto(saved)));
    }


    public Mono<Workshop> updateWorkshop(Workshop workshop) {
        return workshopRepository.save(workshop)
                .doOnNext(updated -> kafkaProducerService.sendWorkshopEvent(toEventDto(updated)));
    }

    public Mono<Workshop> save(Workshop workshop) {
        return workshopRepository.save(workshop);
    }

    public Mono<Void> deleteById(Long id) {
        return workshopRepository.findById(id)
                .flatMap(workshop -> {
                    kafkaProducerService.sendWorkshopEvent(toEventDto(workshop)); // Enviar antes de borrar
                    return workshopRepository.deleteById(id);
                });
    }

    public Mono<Workshop> restoreWorkshop(Long id) {
        return workshopRepository.findById(id)
                .flatMap(workshop -> {
                    if ("I".equals(workshop.getState())) {
                        workshop.setState("A");
                        return workshopRepository.save(workshop)
                                .doOnNext(updated -> kafkaProducerService.sendWorkshopEvent(toEventDto(updated)));
                    }
                    return Mono.just(workshop);
                });
    }

    public Mono<Workshop> logicalDelete(Long id) {
        return workshopRepository.findById(id)
                .flatMap(workshop -> {
                    workshop.setState("I");
                    return workshopRepository.save(workshop)
                            .doOnNext(updated -> kafkaProducerService.sendWorkshopEvent(toEventDto(updated)));
                });
    }

    private WorkshopKafkaEventDto toEventDto(Workshop workshop) {
        WorkshopKafkaEventDto dto = new WorkshopKafkaEventDto();
        dto.setId(workshop.getId());
        dto.setName(workshop.getName());
        dto.setDescription(workshop.getDescription());
        dto.setStartDate(workshop.getStartDate());
        dto.setEndDate(workshop.getEndDate());
        dto.setObservation(workshop.getObservation());
        dto.setState(workshop.getState());
        dto.setPersonId(workshop.getPersonId());
        return dto;
    }

}
