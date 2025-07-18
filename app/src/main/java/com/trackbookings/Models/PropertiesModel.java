package com.trackbookings.Models;

public class PropertiesModel {

    String name;
    int totalRoom;
    String id;

    public PropertiesModel() {
    }

    public PropertiesModel(String name, int totalRoom) {
        this.name = name;
        this.totalRoom = totalRoom;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }
}
