package com.example.eveant.service.model;

import java.util.Date;
import java.util.List;

public class ServiceDTO {
    private Integer id;
    private String provider;
    private Date lastModification;
    private String name;
    private String category;
    private String description;
    private List<Integer> eventTypes;
    private Long price;
    private int discount;
    private Boolean visible;
    private OfferStatus status;
    private List<String> photos;

    private String specification;
    private Integer maxEngagement;
    private Integer minEngagement;
    private Boolean automation;
    private Integer reservationDeadLine;
    private Integer cancellationPeriod;

    public ServiceDTO() {
        super();
    }

    public ServiceDTO(Integer id, String provider, String name, String description, String category,
                      List<Integer> eventTypes, Long price, int discount, Boolean visible,
                      OfferStatus status, List<String> photos, String specification,
                      Integer maxEngagement, Integer minEngagement, Boolean automation,
                      Integer reservationDeadLine, Integer cancellationPeriod) {

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


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Date getLastModification() {
        return lastModification;
    }

    public void setLastModification(Date lastModification) {
        this.lastModification = lastModification;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Integer> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<Integer> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }
}