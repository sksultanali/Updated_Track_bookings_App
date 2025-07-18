package com.trackbookings.Models;

import java.util.List;

public class BookingResponse {
    private String status;
    private String message;
    private int property_id;
    private String property_name;
    private List<BookingDateData> data;

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

    public int getProperty_id() {
        return property_id;
    }

    public void setProperty_id(int property_id) {
        this.property_id = property_id;
    }

    public String getProperty_name() {
        return property_name;
    }

    public void setProperty_name(String property_name) {
        this.property_name = property_name;
    }

    public List<BookingDateData> getData() {
        return data;
    }

    public void setData(List<BookingDateData> data) {
        this.data = data;
    }

    public class Room {
        private int room_no;
        private Booking booking;

        // Getters and Setters

        public int getRoom_no() {
            return room_no;
        }

        public void setRoom_no(int room_no) {
            this.room_no = room_no;
        }

        public Booking getBooking() {
            return booking;
        }

        public void setBooking(Booking booking) {
            this.booking = booking;
        }
    }

    public class BookingDateData {
        private String date;
        private String formatted_date;
        private List<Room> rooms;

        // Getters and Setters

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getFormatted_date() {
            return formatted_date;
        }

        public void setFormatted_date(String formatted_date) {
            this.formatted_date = formatted_date;
        }

        public List<Room> getRooms() {
            return rooms;
        }

        public void setRooms(List<Room> rooms) {
            this.rooms = rooms;
        }
    }



}
