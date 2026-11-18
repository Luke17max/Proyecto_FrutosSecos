package cl.oylfrutossecos.servicio_pedidos.service;

import cl.oylfrutossecos.servicio_pedidos.model.EstadoPedido;
import cl.oylfrutossecos.servicio_pedidos.model.Pedido;
import cl.oylfrutossecos.servicio_pedidos.model.Usuario;
import cl.oylfrutossecos.servicio_pedidos.repository.PedidoRepository;
import cl.oylfrutossecos.servicio_pedidos.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaHoraDesc();
    }

    public List<Pedido> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByFechaHoraDesc(clienteId);
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public Optional<Pedido> buscarPorNumero(String numeroPedido) {
        return pedidoRepository.findByNumeroPedido(numeroPedido);
    }

    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }

    @Transactional
    public Pedido crearPedido(Pedido pedido) {
        if (pedido.getNumeroPedido() == null || pedido.getNumeroPedido().isBlank()) {
            pedido.setNumeroPedido("PED-" + System.currentTimeMillis());
        }
        if (pedido.getFechaHora() == null || pedido.getFechaHora().isBlank()) {
            pedido.setFechaHora(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        if (pedido.getEstado() == null) {
            pedido.setEstado(EstadoPedido.PENDIENTE_VALIDACION);
        }
        if (pedido.getItems() != null) {
            pedido.getItems().forEach(item -> item.setPedido(pedido));
        }
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Optional<Pedido> actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        return pedidoRepository.findById(id).map(pedido -> {
            pedido.setEstado(nuevoEstado);
            return pedidoRepository.save(pedido);
        });
    }

    // Gestion de Usuarios / Autenticacion
    public Optional<Usuario> autenticar(String identificador, String contrasena) {
        return usuarioRepository.findByAliasOrCorreo(identificador, identificador)
                .filter(u -> u.getContrasena().equals(contrasena));
    }

    public Usuario registrarUsuario(Usuario usuario) {
        if (usuarioRepository.existsByAlias(usuario.getAlias())) {
            throw new IllegalArgumentException("El alias ya esta en uso");
        }
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}
