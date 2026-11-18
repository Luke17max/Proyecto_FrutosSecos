package cl.oylfrutossecos.servicio_catalogo.controller;

import cl.oylfrutossecos.servicio_catalogo.model.Categoria;
import cl.oylfrutossecos.servicio_catalogo.model.Producto;
import cl.oylfrutossecos.servicio_catalogo.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos(
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) String buscar
    ) {
        if (buscar != null && !buscar.isBlank()) {
            return ResponseEntity.ok(productoService.buscarPorNombre(buscar.trim()));
        }
        if (categoria != null) {
            return ResponseEntity.ok(productoService.filtrarPorCategoria(categoria));
        }
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/qr/{codigoQr}")
    public ResponseEntity<Producto> buscarPorQr(@PathVariable String codigoQr) {
        return productoService.buscarPorCodigoQr(codigoQr)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stock-critico")
    public ResponseEntity<List<Producto>> listarStockCritico(@RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(productoService.listarStockCritico(limite));
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        producto.setId(null);
        Producto creado = productoService.guardar(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return productoService.buscarPorId(id).map(existente -> {
            producto.setId(id);
            return ResponseEntity.ok(productoService.guardar(producto));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> ajustarStock(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        Integer delta = payload.get("delta");
        if (delta == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe incluir el campo 'delta'"));
        }
        try {
            return productoService.ajustarStock(id, delta)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (productoService.eliminarLogico(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
