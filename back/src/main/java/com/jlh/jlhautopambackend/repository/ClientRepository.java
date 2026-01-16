package com.jlh.jlhautopambackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jlh.jlhautopambackend.modeles.Client;
import java.util.Optional;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Integer> {
    Optional<Client> findByEmailIgnoreCase(String email);

    List<Client> findByAnonymizedFalse();
}
