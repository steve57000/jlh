package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.DemandeTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeTimelineRepository extends JpaRepository<DemandeTimeline, Long> {
}
