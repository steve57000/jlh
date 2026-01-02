package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.DemandeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeDocumentRepository extends JpaRepository<DemandeDocument, Long> {

    List<DemandeDocument> findByDemande_IdDemandeOrderByCreeLeDesc(Integer demandeId);

    Optional<DemandeDocument> findByIdDocumentAndDemande_IdDemande(Long documentId, Integer demandeId);
}
