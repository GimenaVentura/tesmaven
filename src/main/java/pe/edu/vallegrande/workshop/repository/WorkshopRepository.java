package pe.edu.vallegrande.workshop.repository;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.workshop.model.Workshop;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface WorkshopRepository extends ReactiveCrudRepository<Workshop, Long>{
    Flux<Workshop> findAllByState(String state);

    @Modifying
    @Query("update workshop set state = 'I' where id = id")
    Mono<Void> inactiveWorkshop(Long id);

    @Modifying
    @Query("update workshop set state = 'A' where id = id")
    Mono<Void> activateWorkshop(Long id);

    Flux<Workshop> findByPersonId(Long personId);

    Mono<Workshop> findTopByOrderByIdDesc();
}
