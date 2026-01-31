package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.Creneau;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreneauRepository extends JpaRepository<Creneau, Integer> {
    @Query("""
            select c from Creneau c
            where c.dateDebut < :end
              and c.dateFin > :start
            """)
    List<Creneau> findOverlapping(@Param("start") Instant start, @Param("end") Instant end);
}
