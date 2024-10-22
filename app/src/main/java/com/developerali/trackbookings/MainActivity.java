package com.developerali.trackbookings;

import static com.developerali.trackbookings.CalenderHelper.daysInMonthArray;
import static com.developerali.trackbookings.CalenderHelper.daysInWeekArray;
import static com.developerali.trackbookings.CalenderHelper.monthYearFromDate;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.trackbookings.Adapters.CalendarAdapter;
import com.developerali.trackbookings.Models.PropertiesModel;
import com.developerali.trackbookings.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.SelectionListner {

    ActivityMainBinding binding;
    ArrayList<PropertiesModel> modelArrayList;
    int selectedProperty = 2;
    FirebaseDatabase database;
    PropertiesModel[] selectedClient;
    ProgressDialog progressDialog;
    LocalDate today;
    boolean monthView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        today = LocalDate.now();

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("updating booking...");

        modelArrayList = new ArrayList<>();
        modelArrayList.clear();
        modelArrayList.add(new PropertiesModel("Kaffergaon", 3));
        modelArrayList.add(new PropertiesModel("Kolakham", 5));
        modelArrayList.add(new PropertiesModel("Charkhole", 4));
        modelArrayList.add(new PropertiesModel("Sittong", 4));
        modelArrayList.add(new PropertiesModel("Pedong", 4));

        CalenderHelper.selectedDate = LocalDate.now();
        setWeekView();

        ArrayAdapter<PropertiesModel> adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item, modelArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);

        selectedClient = new PropertiesModel[1];
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.optionMenu.setVisibility(View.GONE);
                selectedClient[0] = modelArrayList.get(position);
                if (selectedClient[0] != null) {
                    selectedProperty = position;
                    setWeekView();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optionally handle the scenario when nothing is selected
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
        binding.monthYearTV.setText(monthYearFromDate(CalenderHelper.selectedDate));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(CalenderHelper.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(MainActivity.this, daysInMonth,this, modelArrayList.get(selectedProperty));

        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        binding.calendarRecyclerView.setLayoutManager(layoutManager);
        binding.calendarRecyclerView.setAdapter(calendarAdapter);
        calendarAdapter.notifyDataSetChanged();
    }

    private void setWeekView() {
        //binding.totalPresent.setText("");
        binding.monthYearTV.setText(monthYearFromDate(CalenderHelper.selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(CalenderHelper.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(MainActivity.this, days,this, modelArrayList.get(selectedProperty));

        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        binding.calendarRecyclerView.setLayoutManager(layoutManager);
        binding.calendarRecyclerView.setAdapter(calendarAdapter);
        calendarAdapter.notifyDataSetChanged();
    }


    @Override
    public void onShowAction(Boolean isSelected) {
        // Handle day click
        //Toast.makeText(this, isSelected+"", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoomSelected(String roomName, LocalDate localDate) {
        if (localDate.isBefore(today)) {
            //binding.optionMenu.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Date has expired...!", Toast.LENGTH_SHORT).show();
        }else {
            binding.optionMenu.setVisibility(View.VISIBLE);
            binding.selectedDateRoom.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.blink));
        }
        checkChild(localDate,roomName);
        binding.selectedDateRoom.setText("Selected: "+ localDate.getDayOfWeek().toString().toLowerCase() +
                "\n" + CalenderHelper.formattedDate(localDate) + " | " + roomName);

        binding.saveBtn.setOnClickListener(v->{
            //Toast.makeText(this, CalenderHelper.formattedDate(localDate) + " | " + roomName + " clicked", Toast.LENGTH_SHORT).show();
            progressDialog.show();
            database.getReference().child("booking")
                    .child(selectedClient[0].getName())
                    .child(CalenderHelper.dateKey(localDate))
                    .child(roomName)
                    .setValue(true)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            setWeekView();
                            Toast.makeText(MainActivity.this, "updated booking...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            binding.optionMenu.setVisibility(View.GONE);
        });

        binding.deleteBtn.setOnClickListener(v->{
            //Toast.makeText(this, CalenderHelper.formattedDate(localDate) + " | " + roomName + " clicked", Toast.LENGTH_SHORT).show();

            progressDialog.show();
            database.getReference().child("booking")
                    .child(selectedClient[0].getName())
                    .child(CalenderHelper.dateKey(localDate))
                    .child(roomName)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            setWeekView();
                            Toast.makeText(MainActivity.this, "removed successful...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            binding.optionMenu.setVisibility(View.GONE);
        });



    }

    void checkChild(LocalDate date,String rName){
        database.getReference().child("booking")
                .child(selectedClient[0].getName())
                .child(CalenderHelper.dateKey(date))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.child(rName).exists()){
                                boolean booked = snapshot.child(rName).getValue(Boolean.class);
                                if (booked){
                                    binding.deleteBtn.setVisibility(View.VISIBLE);
                                }else {
                                    binding.deleteBtn.setVisibility(View.INVISIBLE);
                                }
                            }else {
                                binding.deleteBtn.setVisibility(View.INVISIBLE);
                            }
                        }else {
                            binding.deleteBtn.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.deleteBtn.setVisibility(View.INVISIBLE);
                    }
                });
    }


}