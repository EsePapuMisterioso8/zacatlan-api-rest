
package unam.fes.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import unam.fes.entity.vendedor.Vendedor;

import java.util.List;
import java.util.Optional;

public interface VendedorRepository extends JpaRepository<Vendedor, Long> {

    List<Vendedor> findByEstadoSolicitud(String estadoSolicitud);

    boolean existsByUsernameSolicitado(String usernameSolicitado);

    Optional<Vendedor> findByUsuario_Username(String username);

    boolean existsByBloqueId(Long idBloque);

    boolean existsByTelefono(String telefono);

    @Query("""
        SELECT v FROM Vendedor v
        WHERE v.estadoSolicitud = 'APROBADA'
        AND LOWER(CONCAT(CONCAT(v.nombre, ' '), v.apellido))
            LIKE LOWER(CONCAT('%', :texto, '%'))
        ORDER BY v.nombre ASC, v.apellido ASC
    """)
    List<Vendedor> buscarAprobadosPorNombre(
            @Param("texto") String texto,
            Pageable pageable
    );
}
