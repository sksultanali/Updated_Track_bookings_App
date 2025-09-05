package com.trackbookings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trackbookings.Adapters.CalendarAdapter;
import com.trackbookings.Helpers.ApiService;
import com.trackbookings.Helpers.Helpers;
import com.trackbookings.Helpers.RetrofitClient;
import com.trackbookings.Models.ApiResponse;
import com.trackbookings.Models.Booking;
import com.trackbookings.Models.BookingResponse;
import com.trackbookings.Models.PropertiesResponse;
import com.trackbookings.R;
import com.trackbookings.databinding.ActivityMainBinding;
import com.trackbookings.databinding.DailogAddDataBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    int selectedProperty = 2;
    FirebaseDatabase database;
    PropertiesResponse.Location[] selectedClient;
    ProgressDialog progressDialog;
    LocalDate today;
    boolean monthView = true;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        EdgeToEdge.enable(this);
        Helpers.setStatusBarTheme(this, R.color.blue_purple, false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        database = FirebaseDatabase.getInstance();
        today = LocalDate.now();

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("updating booking...");

        binding.logOut.setOnClickListener(v->{
            Helpers.showActionDialog(MainActivity.this, "Logout", "Are you sure want to logout this account? ",
                    "Yes", "No", true, new Helpers.DialogButtonClickListener() {
                        @Override
                        public void onYesButtonClicked() {
                            Helpers.saveTextToSharedPref(MainActivity.this, "name", null);
                            Helpers.saveTextToSharedPref(MainActivity.this, "id", null);
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }

                        @Override
                        public void onNoButtonClicked() {

                        }

                        @Override
                        public void onCloseButtonClicked() {

                        }
                    });
        });

        Call<PropertiesResponse> call = apiService.fetchProperties();
        call.enqueue(new Callback<PropertiesResponse>() {
            @Override
            public void onResponse(Call<PropertiesResponse> call, Response<PropertiesResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    PropertiesResponse propertiesResponse = response.body();
                    if (propertiesResponse.getStatus().equalsIgnoreCase("success")){
                        ArrayAdapter<PropertiesResponse.Location> adapter = new ArrayAdapter<>(MainActivity.this,
                                android.R.layout.simple_spinner_item, propertiesResponse.getData());
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinner.setAdapter(adapter);

                        selectedClient = new PropertiesResponse.Location[1];
                        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                binding.optionMenu.setVisibility(View.GONE);
                                selectedClient[0] = propertiesResponse.getData().get(position);
                                if (selectedClient[0] != null) {
                                    selectedProperty = position;
                                    setMonthView();
                                }
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Optionally handle the scenario when nothing is selected
                            }
                        });

                        selectedClient[0] = propertiesResponse.getData().get(0);
                        CalenderHelper.selectedDate = LocalDate.now();
                        setMonthView();

                    }else {
                        Toast.makeText(MainActivity.this, propertiesResponse.getStatus() + ": "
                                + propertiesResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<PropertiesResponse> call, Throwable t) {

            }
        });

        binding.previousDateBtn.setOnClickListener(v->{
            binding.optionMenu.setVisibility(View.GONE);
            if (monthView == true){
                CalenderHelper.selectedDate = CalenderHelper.selectedDate.minusMonths(1);
                setMonthView();
            }else {
                CalenderHelper.selectedDate = CalenderHelper.selectedDate.minusWeeks(1);
                setWeekView();
            }
        });

        binding.nextDateBtn.setOnClickListener(v->{
            binding.optionMenu.setVisibility(View.GONE);
            if (monthView == true){
                CalenderHelper.selectedDate = CalenderHelper.selectedDate.plusMonths(1);
                setMonthView();
            }else {
                CalenderHelper.selectedDate = CalenderHelper.selectedDate.plusWeeks(1);
                setWeekView();
            }

        });

        binding.cancelBtn.setOnClickListener(v->{
            binding.optionMenu.setVisibility(View.GONE);
        });

        binding.changeView.setOnClickListener(v->{
            if (monthView){
                binding.changeView.setImageResource(R.drawable.week_image_24);
                setWeekView();
                monthView = false;
            }else {
                setMonthView();
                binding.changeView.setImageResource(R.drawable.calendar_month_24);
                monthView = true;
            }
        });



    }

    public void setMonthView() {
        //binding.totalPresent.setText("");
        binding.monthYearTV.setText(CalenderHelper.monthYearFromDate(CalenderHelper.selectedDate));
        ArrayList<LocalDate> daysInMonth = CalenderHelper.daysInMonthArray(CalenderHelper.selectedDate);
        setAdapter(daysInMonth);
    }

    private void setWeekView() {
        //binding.totalPresent.setText("");
        binding.monthYearTV.setText(CalenderHelper.monthYearFromDate(CalenderHelper.selectedDate));
        ArrayList<LocalDate> days = CalenderHelper.daysInWeekArray(CalenderHelper.selectedDate);
        setAdapter(days);
    }

    void setAdapter(ArrayList<LocalDate> days){
        // Initialize with multi-select mode enabled

        Call<BookingResponse> call = apiService.fetchBookings(
                Helpers.formatLocalDateToYYYYMMDD(days.get(0)),
                selectedClient[0].getId(),
                String.valueOf(days.size())
        );

        progressDialog.show();
        call.enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    BookingResponse bookingResponse = response.body();
                    progressDialog.dismiss();
                    if (bookingResponse.getStatus().equalsIgnoreCase("success")){
                        CalendarAdapter calendarAdapter = new CalendarAdapter(
                                MainActivity.this,
                                days,
                                new CalendarAdapter.SelectionListener() {
                                    @Override
                                    public void onSelectionChanged(int totalSelected, Map<LocalDate, ArrayList<String>> selectedItems,
                                                                   Booking booking) {
                                        // Update UI with total selected rooms

                                        if (totalSelected > 0){
                                            binding.selectLayout.setVisibility(View.VISIBLE);
                                            binding.toolBar.setVisibility(View.GONE);
                                        }else {
                                            binding.selectLayout.setVisibility(View.GONE);
                                            binding.toolBar.setVisibility(View.VISIBLE);
                                        }

                                        binding.textView.setText(totalSelected + " Selected");
                                        binding.doneAll.setOnClickListener(v->{
                                            // Use selectedItems map as needed
                                            updateOnDatabase(totalSelected, selectedItems, days, booking);
                                        });

                                        binding.pencilBooking.setOnClickListener(v->{
                                            Helpers.showActionDialog(MainActivity.this,
                                                    "Pencil Booking",
                                                    "All selected <b>room(" + totalSelected + ")</b> will be saved as pencil booking. Are you sure want to do this?",
                                                    "Yes", null, true, new Helpers.DialogButtonClickListener() {
                                                        @Override
                                                        public void onYesButtonClicked() {
                                                            saveDate(selectedItems, days, booking, "PencilBooking", null, null, null, null);
                                                        }

                                                        @Override
                                                        public void onNoButtonClicked() {

                                                        }

                                                        @Override
                                                        public void onCloseButtonClicked() {

                                                        }
                                                    });
                                        });

                                        binding.resetAll.setOnClickListener(v->{
                                            Helpers.showActionDialog(MainActivity.this,
                                                    "Reset Room",
                                                    "All selected <b>room(" + totalSelected + ")</b> will be restored as 0 booking. Are you sure want to do this?",
                                                    "Yes", null, true, new Helpers.DialogButtonClickListener() {
                                                        @Override
                                                        public void onYesButtonClicked() {
                                                            resetDate(selectedItems, days);
                                                        }

                                                        @Override
                                                        public void onNoButtonClicked() {

                                                        }

                                                        @Override
                                                        public void onCloseButtonClicked() {

                                                        }
                                                    });
                                        });

                                    }

                                    @Override
                                    public void onRecordUpdate() {
                                        setAdapter(days);
                                    }
                                },
                                bookingResponse.getData(),
                                true // Enable multi-select mode
                        );

                        binding.clearAll.setOnClickListener(v->{
                            calendarAdapter.clearAllSelections();
                            binding.selectLayout.setVisibility(View.GONE);
                            binding.toolBar.setVisibility(View.VISIBLE);
                        });

                        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

                        binding.calendarRecyclerView.setLayoutManager(layoutManager);
                        binding.calendarRecyclerView.setAdapter(calendarAdapter);
                        int monthPos = new Date().getMonth();
                        int selMonthPos = CalenderHelper.selectedDate.getMonth().getValue() - 1;

                        //Toast.makeText(MainActivity.this, "month Pos "+ monthPos , Toast.LENGTH_SHORT).show();
                        //Toast.makeText(MainActivity.this, "Sel Pos "+ selMonthPos , Toast.LENGTH_SHORT).show();

                        if (monthView == true && selMonthPos == monthPos){
                            int position = new Date().getDate() - 1;
                            LinearLayoutManager layoutManager1 = (LinearLayoutManager) binding.calendarRecyclerView.getLayoutManager();
                            if (layoutManager1 != null) {
                                layoutManager1.scrollToPositionWithOffset(position, 0); // Scroll to position with no offset
                            } else {
                                binding.calendarRecyclerView.smoothScrollToPosition(position);
                            }
                        }
                        calendarAdapter.notifyDataSetChanged();
                    }else {
                        Toast.makeText(MainActivity.this, bookingResponse.getStatus() + ": "
                                + bookingResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateOnDatabase(int totalSelected, Map<LocalDate, ArrayList<String>> selectedItems,
                                  ArrayList<LocalDate> days, Booking booking) {
        DailogAddDataBinding dialogBinding = DailogAddDataBinding.inflate(getLayoutInflater());
        // Create a new dialog and set the custom layout
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogBinding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setStatusBarColor(getColor(R.color.white));
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        dialogBinding.closeBtn.setOnClickListener(v->{
            dialog.dismiss();
        });

        dialogBinding.idName.setText("By "+ Helpers.getTextFromSharedPref(MainActivity.this, "name"));

        if (totalSelected > 1) {
            dialogBinding.dateTime.setText(totalSelected + " Rooms will effected..!");
        }else {
            dialogBinding.dateTime.setText("Single room will effected..!");
        }

        if (booking == null){
            dialogBinding.deleteBtn.setVisibility(View.INVISIBLE);
        }else {
            dialogBinding.deleteBtn.setVisibility(View.VISIBLE);
        }

        dialogBinding.deleteBtn.setVisibility(View.GONE);
        dialogBinding.saveBtn.setOnClickListener(v -> {
            String bookingName = dialogBinding.bookingName.getText().toString();
            String phone = dialogBinding.bookingPhone.getText().toString();
            String pax = dialogBinding.pax.getText().toString();
            String due = dialogBinding.due.getText().toString();

            if (bookingName.isEmpty()){
                dialogBinding.bookingName.setError("*");
                return;
            }
            if (phone.isEmpty()){
                dialogBinding.bookingPhone.setError("*");
                return;
            }
            if (pax.isEmpty()){
                dialogBinding.pax.setError("*");
                return;
            }
            if (due.isEmpty()){
                dialogBinding.due.setError("*");
                return;
            }
            String remark = dialogBinding.remarks.getText().toString();
            dialog.dismiss();
            saveDate(selectedItems, days, booking, bookingName, remark, pax, due, phone);
        });

        dialog.show();
    }

    void saveDate(Map<LocalDate, ArrayList<String>> selectedItems,
                  ArrayList<LocalDate> days, Booking booking, String bookingName, String remark, String pax, String due, String phone){
        List<Call<ApiResponse>> apiCalls = new ArrayList<>();
        List<String> failedRooms = new ArrayList<>();
        List<String> successRooms = new ArrayList<>();
        int[] completedCount = {0};  // To track responses
        int totalCalls = 0;

        progressDialog.show();

        // Step 1: Prepare all calls
        for (Map.Entry<LocalDate, ArrayList<String>> entry : selectedItems.entrySet()) {
            LocalDate date = entry.getKey();
            ArrayList<String> rooms = entry.getValue();
            String formattedDate = CalenderHelper.dateKey(date);

            for (String room : rooms) {
                Call<ApiResponse> call = apiService.addBooking(
                        formattedDate,
                        selectedClient[0].getId(),
                        room,
                        Helpers.getTextFromSharedPref(MainActivity.this, "id"),
                        bookingName,
                        pax,
                        due,
                        remark,
                        phone
                );
                apiCalls.add(call);
            }
        }

        totalCalls = apiCalls.size();

        // Step 2: Make all API calls and track
        for (Call<ApiResponse> call : apiCalls) {
            int finalTotalCalls = totalCalls;
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && "success".equalsIgnoreCase(response.body().getStatus())) {
                        // Success - do nothing here
                        String message = "Update Successful";
                        if (response.body() != null && response.body().getMessage() != null) {
                            message = response.body().getMessage();
                        }
                        successRooms.add("Success: " + message);
                    } else {
                        String message = "Internal Error";
                        if (response.body() != null && response.body().getMessage() != null) {
                            message = response.body().getMessage();
                        }
                        failedRooms.add("Failed: " + message);
                    }
                    checkCompletion();
                }


                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    failedRooms.add("Error: " + t.getLocalizedMessage());
                    checkCompletion();
                }

                // Step 3: Check if all done
                private void checkCompletion() {
                    completedCount[0]++;
                    if (completedCount[0] == finalTotalCalls) {
                        progressDialog.dismiss();
                        binding.clearAll.performClick();
                        setAdapter(days);

                        if (failedRooms.isEmpty() && !successRooms.isEmpty()) {
                            StringBuilder msg = new StringBuilder();
                            for (String err : successRooms) {
                                msg.append("- ").append(err).append("\n\n");
                            }
                            showFailureDialog(msg.toString(), "Bookings Update Successful");
                            Toast.makeText(MainActivity.this, "All bookings completed successfully!", Toast.LENGTH_LONG).show();
                        } else {
                            StringBuilder msg = new StringBuilder();
                            for (String err : failedRooms) {
                                msg.append("- ").append(err).append("\n\n");
                            }
                            showFailureDialog(msg.toString(), "Booking Errors");
                        }
                    }
                }
            });
        }
    }

    void resetDate(Map<LocalDate, ArrayList<String>> selectedItems,
                  ArrayList<LocalDate> days){
        List<Call<ApiResponse>> apiCalls = new ArrayList<>();
        List<String> failedRooms = new ArrayList<>();
        List<String> successRooms = new ArrayList<>();
        int[] completedCount = {0};  // To track responses
        int totalCalls = 0;

        progressDialog.show();

        // Step 1: Prepare all calls
        for (Map.Entry<LocalDate, ArrayList<String>> entry : selectedItems.entrySet()) {
            LocalDate date = entry.getKey();
            ArrayList<String> rooms = entry.getValue();
            String formattedDate = CalenderHelper.dateKey(date);

            for (String room : rooms) {
                Call<ApiResponse> call = apiService.deleteBooking(
                        formattedDate, Helpers.getTextFromSharedPref(MainActivity.this, "id"), selectedClient[0].getId(),
                        room
                );
                //Toast.makeText(this, "Prop Id " + selectedClient[0].getId(), Toast.LENGTH_SHORT).show();
                apiCalls.add(call);
            }
        }

        totalCalls = apiCalls.size();

        // Step 2: Make all API calls and track
        for (Call<ApiResponse> call : apiCalls) {
            int finalTotalCalls = totalCalls;
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (!(response.isSuccessful() && response.body() != null && "success".equalsIgnoreCase(response.body().getStatus()))) {
                        String message = response.body().getMessage() != null ? response.body().getMessage() : "Internal Error";
                        failedRooms.add("Failed: " + message);
                    }else {
                        String message = response.body().getMessage() != null ? response.body().getMessage() : "Reset Complete";
                        successRooms.add("Success: " + message + " And date " + Helpers.formatDate(response.body().getDate(),
                                "yyyyMMdd", "dd LLL yyyy"));
                    }
                    checkCompletion();
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    failedRooms.add("Error: " + t.getLocalizedMessage());
                    checkCompletion();
                }

                // Step 3: Check if all done
                private void checkCompletion() {
                    completedCount[0]++;
                    if (completedCount[0] == finalTotalCalls) {
                        progressDialog.dismiss();
                        binding.clearAll.performClick();
                        setAdapter(days);

                        if (failedRooms.isEmpty() && !successRooms.isEmpty()) {
                            StringBuilder msg = new StringBuilder();
                            for (String err : successRooms) {
                                msg.append("- ").append(err).append("\n\n");
                            }
                            showFailureDialog(msg.toString(), "Reset Complete");
                            //Toast.makeText(MainActivity.this, "All bookings completed successfully!", Toast.LENGTH_LONG).show();
                        } else {
                            StringBuilder msg = new StringBuilder();
                            for (String err : failedRooms) {
                                msg.append("- ").append(err).append("\n\n");
                            }
                            showFailureDialog(msg.toString(), "Booking Errors");
                        }
                    }
                }
            });
        }
    }

    // Optional: Show failure message nicely
    private void showFailureDialog(String message, String title) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

}