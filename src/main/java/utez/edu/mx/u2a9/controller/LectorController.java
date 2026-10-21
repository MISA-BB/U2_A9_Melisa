package utez.edu.mx.u2a9.controller;

import utez.edu.mx.u2a9.service.PrestamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lectores")
@RequiredArgsConstructor
public class LectorController {

    private final PrestamoService prestamoService;

    @PutMapping("/{id}/rehabilitar")
    public ResponseEntity<Void> rehabilitarLector(@PathVariable Long id) {
        prestamoService.rehabilitarLector(id);
        return ResponseEntity.ok().build();
    }
}