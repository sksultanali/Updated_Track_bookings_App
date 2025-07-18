package com.trackbookings.Models;

import java.util.List;

public class PropertiesResponse {
    private String status;
    private String message;
    private List<Location> data;

    // Getter and Setter
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Location> getData() {
        return data;
    }

    public void setData(List<Location> data) {
        this.data = data;
    }

    public class Location {
        private String id;
        private String name;
        private int totalRoom;
        private String img; // Since it can be null

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTotalRoom() {
            return totalRoom;
        }

        public void setTotalRoom(int totalRoom) {
            this.totalRoom = totalRoom;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        @Override
        public String toString() {
            return name + " ("+ totalRoom +")";
        }
    }

}
