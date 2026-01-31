package com.jlh.jlhautopambackend.dto;

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
public class AvisServiceModerationRequest {
    @NotNull
    private String statut;

    @Size(max = 1000)
    private String motifRefus;
}
