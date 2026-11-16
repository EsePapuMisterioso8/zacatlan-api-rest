package unam.fes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unam.fes.entity.vendedor.Vendedor;

import java.util.List;
import java.util.Optional;

public interface VendedorRepository extends JpaRepository<Vendedor,Long> {


    List<Vendedor> findByEstadoSolicitud(String estadoSolicitud);
    boolean existsByUsernameSolicitado(String usernameSolicitado);
    Optional<Vendedor> findByUsuario_Username(String username);
    boolean existsByBloqueId(Long idBloque);
    boolean existsByTelefono(String telefono);
}
