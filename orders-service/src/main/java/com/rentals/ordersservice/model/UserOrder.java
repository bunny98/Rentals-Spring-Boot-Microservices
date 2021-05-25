package com.rentals.ordersservice.model;

import org.springframework.data.annotation.Id;

import java.util.List;

public class UserOrder {
    @Id
    private String id;
    private List<String> sentReqProductIds;
    private List<String> receivedReqProductIds;

    @Override
    public String toString() {
        return "UserOrder{" +
                "id='" + id + '\'' +
                ", sentReqProductIds=" + sentReqProductIds +
                ", receivedReqProductIds=" + receivedReqProductIds +
                '}';
    }

    public UserOrder(String id, List<String> sentReqProductIds, List<String> receivedReqProductIds) {
        this.id = id;
        this.sentReqProductIds = sentReqProductIds;
        this.receivedReqProductIds = receivedReqProductIds;
    }

    public UserOrder() {
    }

    public String getId() {
        return id;
    }

    public List<String> getSentReqProductIds() {
        return sentReqProductIds;
    }

    public List<String> getReceivedReqProductIds() {
        return receivedReqProductIds;
    }

    public void setSentReqProductIds(List<String> sentReqProductIds) {
        this.sentReqProductIds = sentReqProductIds;
    }

    public void setReceivedReqProductIds(List<String> receivedReqProductIds) {
        this.receivedReqProductIds = receivedReqProductIds;
    }
}
