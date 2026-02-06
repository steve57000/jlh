package com.jlh.jlhautopambackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisServiceRequest {
    @NotNull
    private Integer demandeId;

    @NotNull
    private Integer serviceId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer note;

    @Size(max = 1000)
    private String commentaire;
}
