package cl.oylfrutossecos.servicio_pedidos.controller;

import cl.oylfrutossecos.servicio_pedidos.model.EstadoPedido;
import cl.oylfrutossecos.servicio_pedidos.model.Pedido;
import cl.oylfrutossecos.servicio_pedidos.model.Usuario;
import cl.oylfrutossecos.servicio_pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listarPedidos(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) EstadoPedido estado
    ) {
        if (clienteId != null) {
            return ResponseEntity.ok(pedidoService.listarPorCliente(clienteId));
        }
        if (estado != null) {
            return ResponseEntity.ok(pedidoService.listarPorEstado(estado));
        }
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/numero/{numeroPedido}")
    public ResponseEntity<Pedido> buscarPorNumero(@PathVariable String numeroPedido) {
        return pedidoService.buscarPorNumero(numeroPedido)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Pedido> crearPedido(@Valid @RequestBody Pedido pedido) {
        pedido.setId(null);
        Pedido creado = pedidoService.crearPedido(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload
    ) {
        String estadoStr = payload.get("estado");
        if (estadoStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe incluir el campo 'estado'"));
        }
        try {
            EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoStr.trim());
            return pedidoService.actualizarEstado(id, nuevoEstado)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Estado no valido: " + estadoStr));
        }
    }

    // Endpoints de Usuarios / Auth
    @PostMapping("/usuarios/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String identificador = credenciales.get("identificador");
        String contrasena = credenciales.get("contrasena");
        if (identificador == null || contrasena == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe enviar identificador y contrasena"));
        }
        return pedidoService.autenticar(identificador, contrasena)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/usuarios/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody Usuario usuario) {
        try {
            usuario.setId(null);
            Usuario creado = pedidoService.registrarUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(pedidoService.listarUsuarios());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> buscarUsuarioPorId(@PathVariable Long id) {
        return pedidoService.buscarUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
