package cl.oylfrutossecos.servicio_pedidos.repository;

import cl.oylfrutossecos.servicio_pedidos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByAlias(String alias);
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByAliasOrCorreo(String alias, String correo);
    boolean existsByAlias(String alias);
    boolean existsByCorreo(String correo);
}
