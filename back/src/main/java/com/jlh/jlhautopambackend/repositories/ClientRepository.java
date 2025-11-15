package com.jlh.jlhautopambackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jlh.jlhautopambackend.modeles.Client;

public interface ClientRepository extends JpaRepository<Client, Integer> { }