package com.jlh.jlhautopambackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVousPropositionBatchRequest {
    @Valid
    @NotEmpty
    private List<RendezVousPropositionSlotRequest> propositions;
}
