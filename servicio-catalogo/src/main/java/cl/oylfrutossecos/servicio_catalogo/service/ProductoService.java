package cl.oylfrutossecos.servicio_catalogo.service;

import cl.oylfrutossecos.servicio_catalogo.model.Categoria;
import cl.oylfrutossecos.servicio_catalogo.model.Producto;
import cl.oylfrutossecos.servicio_catalogo.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarTodos() {
        return productoRepository.findByActivoTrue();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Optional<Producto> buscarPorCodigoQr(String codigoQr) {
        return productoRepository.findByCodigoQr(codigoQr);
    }

    public List<Producto> filtrarPorCategoria(Categoria categoria) {
        if (categoria == null || categoria == Categoria.TODOS) {
            return listarTodos();
        }
        return productoRepository.findByCategoriaAndActivoTrue(categoria);
    }

    public List<Producto> buscarPorNombre(String query) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(query);
    }

    public List<Producto> listarStockCritico(int limite) {
        return productoRepository.findByStockLessThanEqualAndActivoTrue(limite);
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    @Transactional
    public Optional<Producto> ajustarStock(Long id, int delta) {
        return productoRepository.findById(id).map(prod -> {
            int nuevoStock = prod.getStock() + delta;
            if (nuevoStock < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo");
            }
            prod.setStock(nuevoStock);
            return productoRepository.save(prod);
        });
    }

    public boolean eliminarLogico(Long id) {
        return productoRepository.findById(id).map(prod -> {
            prod.setActivo(false);
            productoRepository.save(prod);
            return true;
        }).orElse(false);
    }
}
