package com.example.eveant.reservation;

public class Reservation {
    private int id;
    private String serviceName;
    private String reservationTimestamp;
    private int priceAtReservation;
    private String startTime;
    private String endTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getReservationTimestamp() {
        return reservationTimestamp;
    }

    public void setReservationTimestamp(String reservationTimestamp) {
        this.reservationTimestamp = reservationTimestamp;
    }

    public int getPriceAtReservation() {
        return priceAtReservation;
    }

    public void setPriceAtReservation(int priceAtReservation) {
        this.priceAtReservation = priceAtReservation;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


}
