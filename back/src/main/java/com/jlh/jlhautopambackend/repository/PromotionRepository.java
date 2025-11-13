package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    List<Promotion> findByValidToBefore(Instant dateTime);
    void deleteByValidToBefore(Instant dateTime);
}
