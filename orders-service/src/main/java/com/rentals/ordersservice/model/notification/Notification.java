package com.rentals.ordersservice.model.notification;

import java.util.List;

public class Notification {
    private List<Request> sentRequests;
    private List<Request> receivedRequests;

    public Notification(List<Request> sentRequests, List<Request> receivedRequests) {
        this.sentRequests = sentRequests;
        this.receivedRequests = receivedRequests;
    }

    public List<Request> getSentRequests() {
        return sentRequests;
    }

    public List<Request> getReceivedRequests() {
        return receivedRequests;
    }
}
