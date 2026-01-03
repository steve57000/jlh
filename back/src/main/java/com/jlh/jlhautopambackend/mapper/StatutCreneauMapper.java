package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.StatutCreneauDto;
import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatutCreneauMapper {
    StatutCreneau toEntity(StatutCreneauDto dto);
    StatutCreneauDto toDto(StatutCreneau entity);
}