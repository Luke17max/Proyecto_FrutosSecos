package cl.oylfrutossecos.servicio_catalogo.repository;

import cl.oylfrutossecos.servicio_catalogo.model.Categoria;
import cl.oylfrutossecos.servicio_catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigoQr(String codigoQr);

    List<Producto> findByCategoriaAndActivoTrue(Categoria categoria);

    List<Producto> findByActivoTrue();

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String query);

    List<Producto> findByStockLessThanEqualAndActivoTrue(int limiteStock);
}
