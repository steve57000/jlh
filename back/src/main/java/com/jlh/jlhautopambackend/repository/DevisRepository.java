package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.Devis;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DevisRepository extends JpaRepository<Devis, Integer> {
    java.util.Optional<Devis> findByDemande_IdDemande(Integer idDemande);

    @Query("""
        select function('date_part', 'year', d.dateDevis) as year,
               count(d) as count,
               sum(d.montantTotal) as amount
        from Devis d
        group by function('date_part', 'year', d.dateDevis)
    """)
    List<YearlyAmountCount> aggregateYearlyDevisStats();

    interface YearlyAmountCount {
        Integer getYear();
        Long getCount();
        BigDecimal getAmount();
    }
}
