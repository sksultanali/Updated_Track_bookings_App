package com.trackbookings.Helpers;


import androidx.annotation.Nullable;

import com.trackbookings.Models.ApiResponse;
import com.trackbookings.Models.BookingResponse;
import com.trackbookings.Models.LoginRes;
import com.trackbookings.Models.PropertiesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("apis/api.php?action=checkUserCred")
    Call<LoginRes> getLoginDetails(
            @Query("phone") String phone,
            @Query("password") String password
    );

    @GET("apis/api.php?action=addBooking")
    Call<ApiResponse> addBooking(
            @Query("date") String date,
            @Query("property_id") String property_id,
            @Query("room_no") String room_no,
            @Query("user_id") String user_id,
            @Query("booking_name")@Nullable String booking_name,
            @Query("pax")@Nullable String pax,
            @Query("due")@Nullable String due,
            @Query("remark")@Nullable String remark
    );

    @GET("apis/api.php?action=fetchBookings")
    Call<BookingResponse> fetchBookings(
            @Query("date") String date,
            @Query("property_id") String property_id,
            @Query("till_date") String till_date
    );

    @GET("apis/api.php?action=fetchProperties")
    Call<PropertiesResponse> fetchProperties();

    @GET("apis/api.php?action=deleteTable")
    Call<ApiResponse> deleteTableRecord(
            @Query("tableName") String tableName,
            @Query("id") String id
    );

    @GET("apis/api.php?action=editTable")
    Call<ApiResponse> editTable(
            @Query("tableName") String tableName,
            @Query("fieldName") String fieldName,
            @Query("fieldValue")@Nullable String fieldValue,
            @Query("id") String id
    );

    @GET("apis/api.php?action=deleteBooking")
    Call<ApiResponse> deleteBooking(
            @Query("date") String date,
            @Query("user_id") String user_id,
            @Query("prop_id") String prop_id,
            @Query("room_no") String room_no
    );


}
