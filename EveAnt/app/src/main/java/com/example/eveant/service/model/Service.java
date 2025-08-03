package com.example.eveant.service.model;

import com.example.eveant.user.model.Provider;

import java.util.List;

public class Service extends Offer {
    private String specification;
    private Integer maxEngagement;
    private Integer minEngagement;
    private Boolean automation;
    private Integer reservationDeadLine;
    private Integer cancellationPeriod;

    public Service() {
        super();
    }

    public Service(Integer id, Provider provider, String name, String description, Category category,
                   List<EventType> eventTypes, Long price, int discount, Boolean visible,
                   OfferStatus status, List<String> photos, String specification,
                   Integer maxEngagement, Integer minEngagement, Boolean automation,
                   Integer reservationDeadLine, Integer cancellationPeriod) {
        super(id, provider, null, name, category, description, eventTypes, price, discount, visible, status, photos);
        this.specification = specification;
        this.maxEngagement = maxEngagement;
        this.minEngagement = minEngagement;
        this.automation = automation;
        this.reservationDeadLine = reservationDeadLine;
        this.cancellationPeriod = cancellationPeriod;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public Integer getMaxEngagement() {
        return maxEngagement;
    }

    public void setMaxEngagement(Integer maxEngagement) {
        this.maxEngagement = maxEngagement;
    }

    public Integer getMinEngagement() {
        return minEngagement;
    }

    public void setMinEngagement(Integer minEngagement) {
        this.minEngagement = minEngagement;
    }

    public Boolean getAutomation() {
        return automation;
    }

    public void setAutomation(Boolean automation) {
        this.automation = automation;
    }

    public Integer getReservationDeadLine() {
        return reservationDeadLine;
    }

    public void setReservationDeadLine(Integer reservationDeadLine) {
        this.reservationDeadLine = reservationDeadLine;
    }

    public Integer getCancellationPeriod() {
        return cancellationPeriod;
    }

    public void setCancellationPeriod(Integer cancellationPeriod) {
        this.cancellationPeriod = cancellationPeriod;
    }



}
