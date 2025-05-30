package pe.edu.vallegrande.workshop.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WorkshopKafkaEventDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String observation;
    private String state;
    private String personId;
}
