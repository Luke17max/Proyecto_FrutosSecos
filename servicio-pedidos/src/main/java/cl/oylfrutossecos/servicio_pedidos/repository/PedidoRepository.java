package cl.oylfrutossecos.servicio_pedidos.repository;

import cl.oylfrutossecos.servicio_pedidos.model.EstadoPedido;
import cl.oylfrutossecos.servicio_pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByNumeroPedido(String numeroPedido);
    List<Pedido> findByClienteIdOrderByFechaHoraDesc(Long clienteId);
    List<Pedido> findAllByOrderByFechaHoraDesc();
    List<Pedido> findByEstado(EstadoPedido estado);
}
