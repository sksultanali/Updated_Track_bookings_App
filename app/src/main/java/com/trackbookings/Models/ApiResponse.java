package com.trackbookings.Models;

public class ApiResponse {
    private String status;
    private String homeId;
    private String date;
    private String room;
    private String roomId;
    private String message;
    private String affected_rows;
    private boolean valid;

    // Getters
    public String getStatus() {
        return status;
    }

    public String getHomeId() {
        return homeId;
    }

    public String getDate() {
        return date;
    }

    public String getRoom() {
        return room;
    }

    public String getAffected_rows() {
        return affected_rows;
    }

    public String getMessage() {
        return message;
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean isValid() {
        return valid;
    }
}

