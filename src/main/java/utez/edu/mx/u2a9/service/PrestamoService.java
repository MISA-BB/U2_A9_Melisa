package utez.edu.mx.u2a9.service;

import utez.edu.mx.u2a9.dto.PrestamoResponseDTO;
import utez.edu.mx.u2a9.dto.SolicitudPrestamoDTO;
import utez.edu.mx.u2a9.exception.BusinessException;
import utez.edu.mx.u2a9.exception.ResourceNotFoundException;
import utez.edu.mx.u2a9.model.*;
import utez.edu.mx.u2a9.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LectorRepository lectorRepository;
    private final LibroRepository libroRepository;

    private static final List<EstadoPrestamo> ESTADOS_ACTIVOS = List.of(EstadoPrestamo.PRESTADO, EstadoPrestamo.VENCIDO);

    // 1. solicitarPrestamo (35 pts)
    @Transactional
    public PrestamoResponseDTO solicitarPrestamo(SolicitudPrestamoDTO solicitud) {
        Lector lector = lectorRepository.findById(solicitud.getLectorId())
                .orElseThrow(() -> new ResourceNotFoundException("El lector no existe con ID: " + solicitud.getLectorId()));

        if (!lector.isActivo()) {
            throw new BusinessException("El lector se encuentra inactivo o bloqueado.");
        }

        Libro libro = libroRepository.findById(solicitud.getLibroId())
                .orElseThrow(() -> new ResourceNotFoundException("El libro no existe con ID: " + solicitud.getLibroId()));

        if (libro.getEjemplaresDisponibles() < 1) {
            throw new BusinessException("El libro no tiene ejemplares disponibles.");
        }

        long prestamosActivos = prestamoRepository.countPrestamosActivosLector(lector.getId(), ESTADOS_ACTIVOS);
        int limiteMaximo = lector.isMembresiaPremium() ? 5 : 3;

        if (prestamosActivos >= limiteMaximo) {
            throw new BusinessException("El lector ha alcanzado el límite máximo de préstamos activos (" + limiteMaximo + ").");
        }

        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() - 1);
        libroRepository.save(libro);

        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = Prestamo.builder()
                .lector(lector)
                .libro(libro)
                .fechaPrestamo(hoy)
                .fechaDevolucionMaxima(hoy.plusDays(14))
                .estado(EstadoPrestamo.PRESTADO)
                .build();

        return mapToDTO(prestamoRepository.save(prestamo));
    }

    // 2. devolverLibro (30 pts)
    @Transactional
    public PrestamoResponseDTO devolverLibro(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("El préstamo no existe con ID: " + prestamoId));

        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new BusinessException("El libro ya fue devuelto previamente.");
        }

        prestamo.setEstado(EstadoPrestamo.DEVUELTO);

        Libro libro = prestamo.getLibro();
        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() + 1);
        libroRepository.save(libro);

        if (LocalDate.now().isAfter(prestamo.getFechaDevolucionMaxima())) {
            Lector lector = prestamo.getLector();
            lector.setActivo(false);
            lectorRepository.save(lector);
        }

        return mapToDTO(prestamoRepository.save(prestamo));
    }

    // 3. renovarPrestamo (15 pts)
    @Transactional
    public PrestamoResponseDTO renovarPrestamo(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("El préstamo no existe con ID: " + prestamoId));

        if (prestamo.getEstado() == EstadoPrestamo.VENCIDO || prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new BusinessException("No se puede renovar un préstamo que esté en estado VENCIDO o DEVUELTO.");
        }

        prestamo.setFechaDevolucionMaxima(prestamo.getFechaDevolucionMaxima().plusDays(7));
        return mapToDTO(prestamoRepository.save(prestamo));
    }

    // 4. rehabilitarLector (10 pts)
    @Transactional
    public void rehabilitarLector(Long lectorId) {
        Lector lector = lectorRepository.findById(lectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Lector no encontrado con ID: " + lectorId));
        lector.setActivo(true);
        lectorRepository.save(lector);
    }

    // 5. listarPrestamosActivosLector (10 pts)
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> listarPrestamosActivosLector(Long lectorId) {
        if (!lectorRepository.existsById(lectorId)) {
            throw new ResourceNotFoundException("El lector no existe con ID: " + lectorId);
        }
        return prestamoRepository.findPrestamosActivosLector(lectorId, ESTADOS_ACTIVOS)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PrestamoResponseDTO mapToDTO(Prestamo prestamo) {
        return PrestamoResponseDTO.builder()
                .id(prestamo.getId())
                .lectorId(prestamo.getLector().getId())
                .lectorNombre(prestamo.getLector().getNombre())
                .libroId(prestamo.getLibro().getId())
                .libroTitulo(prestamo.getLibro().getTitulo())
                .fechaPrestamo(prestamo.getFechaPrestamo())
                .fechaDevolucionMaxima(prestamo.getFechaDevolucionMaxima())
                .estado(prestamo.getEstado())
                .build();
    }
}