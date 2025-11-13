package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeServiceRequest {
    private Integer demandeId;
    private Integer serviceId;
    private Integer quantite;
}
