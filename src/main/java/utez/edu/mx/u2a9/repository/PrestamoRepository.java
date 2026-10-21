package utez.edu.mx.u2a9.repository;

import utez.edu.mx.u2a9.model.EstadoPrestamo;
import utez.edu.mx.u2a9.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.lector.id = :lectorId AND p.estado IN :estados")
    long countPrestamosActivosLector(@Param("lectorId") Long lectorId, @Param("estados") List<EstadoPrestamo> estados);

    @Query("SELECT p FROM Prestamo p WHERE p.lector.id = :lectorId AND p.estado IN :estados")
    List<Prestamo> findPrestamosActivosLector(@Param("lectorId") Long lectorId, @Param("estados") List<EstadoPrestamo> estados);
}