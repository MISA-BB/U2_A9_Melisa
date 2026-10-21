package utez.edu.mx.u2a9.repository;

import utez.edu.mx.u2a9.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {}