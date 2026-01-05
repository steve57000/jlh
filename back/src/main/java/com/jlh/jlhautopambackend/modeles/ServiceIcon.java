package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "service_icon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceIcon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idIcon;

    @Lob
    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String url;

    @Column(length = 150)
    private String label;
}
