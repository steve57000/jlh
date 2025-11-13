package com.jlh.jlhautopambackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jlh.jlhautopambackend.modeles.Client;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {
    Optional<Client> findByEmail(String email);
}
