package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.ServiceIcon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceIconRepository extends JpaRepository<ServiceIcon, Integer> {
    Optional<ServiceIcon> findByUrl(String url);
}
