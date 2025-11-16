package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.ClientDto;
import com.jlh.jlhautopambackend.modeles.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    ClientDto toDto(Client entity);
}
