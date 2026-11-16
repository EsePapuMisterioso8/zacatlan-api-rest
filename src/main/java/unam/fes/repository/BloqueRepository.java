package unam.fes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unam.fes.entity.bloque.Bloque;

import java.util.List;

public interface BloqueRepository extends JpaRepository<Bloque,Long> {
    List<Bloque> findByActivoTrue();
}
