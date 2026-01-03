package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Integer> {
    List<Service> findAllByArchivedFalseOrderByLibelleAsc();

    Optional<Service> findByIdServiceAndArchivedFalse(Integer idService);

    boolean existsByIdServiceAndArchivedFalse(Integer idService);
}
