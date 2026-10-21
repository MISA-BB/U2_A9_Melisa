package utez.edu.mx.u2a9.dto;

import utez.edu.mx.u2a9.model.EstadoPrestamo;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PrestamoResponseDTO {
    private Long id;
    private Long lectorId;
    private String lectorNombre;
    private Long libroId;
    private String libroTitulo;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionMaxima;
    private EstadoPrestamo estado;
}