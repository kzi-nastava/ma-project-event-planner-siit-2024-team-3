package com.example.eveant.service.model;

import com.example.eveant.user.model.Profile;
import com.example.eveant.user.model.Provider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ServiceMapper {
    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    // Service → ServiceDTO
    @Mapping(source = "provider", target = "provider")
    ServiceDTO toDTO(Service service);


    // ServiceDTO → Service
    @Mapping(source = "provider", target = "provider")
    Service toEntity(ServiceDTO serviceDTO);

    // Category mapping
    default String categoryToString(Category category) {
        return category != null ? category.getName() : null;
    }

    default Category stringToCategory(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return null;
        }
        return new Category(categoryName);
    }

    // Provider mapping
    default String providerToString(Provider provider) {
        if (provider == null || provider.getProfile() == null) {
            return null;
        }
        return provider.getProfile().getUsername();
    }

    default Provider stringToProvider(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        Profile profile = new Profile();
        profile.setUsername(username);

        Provider provider = new Provider();
        provider.setProfile(profile);
        return provider;
    }
}
