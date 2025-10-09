package com.example.eveant.service.model;

import com.example.eveant.service.model.Service;
import com.example.eveant.service.model.ServiceDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ServiceMapper {
    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    @Mapping(source = "category.name", target = "category")
    ServiceDTO toDTO(Service service);

    @Mapping(source = "category", target = "category.name")
    Service toEntity(ServiceDTO serviceDTO);

    default String map(Category category) {
        return category != null ? category.getName() : null;
    }

    default Category map(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return null;
        }
        return new Category(categoryName);
    }
}
