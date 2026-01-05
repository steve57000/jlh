package com.jlh.jlhautopambackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceIconRequest {
    @NotBlank
    @Size(max = 200000)
    private String url;

    @Size(max = 150)
    private String label;
}
