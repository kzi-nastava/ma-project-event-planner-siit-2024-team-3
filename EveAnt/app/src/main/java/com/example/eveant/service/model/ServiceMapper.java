package com.example.eveant.service.model;

import android.util.Log;

import com.example.eveant.eventType.EventType;

import java.util.ArrayList;
import java.util.List;

public class ServiceMapper {

    public static ServiceDTO toDTO(Service service) {
        if (service == null) return null;

        ServiceDTO dto = new ServiceDTO();

        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setDiscount(service.getDiscount() != 0 ? service.getDiscount() : 0);
        dto.setVisible(service.getVisible());
        dto.setStatus(service.getStatus());
        dto.setSpecification(service.getSpecification());
        dto.setMaxEngagement(service.getMaxEngagement());
        dto.setMinEngagement(service.getMinEngagement());
        dto.setAutomation(service.getAutomation());
        dto.setReservationDeadLine(service.getReservationDeadLine());
        dto.setCancellationPeriod(service.getCancellationPeriod());
        dto.setPhotos(service.getPhotos());

        dto.setProvider(service.getProvider()); // TODO: zameni sa stvarnim username-om koji je logovan

        if (service.getCategory() != null) {
            Category catDto = new Category();
            catDto.setName(service.getCategory().getName());
            dto.setCategory(catDto);
        }

        if (service.getEventTypes() != null) {
            Log.d("Event typovi",service.getEventTypes().toString());
            dto.setEventTypes(service.getEventTypes());
        }

        return dto;
    }
}
