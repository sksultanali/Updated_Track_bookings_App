package com.developerali.trackbookings;

import static com.developerali.trackbookings.CalenderHelper.daysInMonthArray;
import static com.developerali.trackbookings.CalenderHelper.daysInWeekArray;
import static com.developerali.trackbookings.CalenderHelper.monthYearFromDate;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.SelectionListner {

    Calendar calendar;
    ActivityMainBinding binding;
    ArrayList<PropertiesModel> modelArrayList;
    int selectedProperty = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        modelArrayList = new ArrayList<>();
        modelArrayList.clear();
        modelArrayList.add(new PropertiesModel("Abc", 3));
        modelArrayList.add(new PropertiesModel("Xyz", 4));
        modelArrayList.add(new PropertiesModel("Pqr", 5));

        CalenderHelper.selectedDate = LocalDate.now();
        setWeekView();

        ArrayAdapter<PropertiesModel> adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item, modelArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);

        PropertiesModel[] selectedClient = new PropertiesModel[1];
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
            CalenderHelper.selectedDate = CalenderHelper.selectedDate.minusMonths(1);
            binding.optionMenu.setVisibility(View.GONE);
            setWeekView();
        });

        binding.nextDateBtn.setOnClickListener(v->{
            CalenderHelper.selectedDate = CalenderHelper.selectedDate.plusMonths(1);
            binding.optionMenu.setVisibility(View.GONE);
            setWeekView();
        });

        binding.cancelBtn.setOnClickListener(v->{
            binding.optionMenu.setVisibility(View.GONE);
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
        Toast.makeText(this, isSelected+"", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoomSelected(String roomName, LocalDate localDate) {
        binding.optionMenu.setVisibility(View.VISIBLE);
        binding.selectedDateRoom.setText("Selected: \n" + CalenderHelper.formattedDate(localDate) + " | " + roomName);

        binding.saveBtn.setOnClickListener(v->{
            Toast.makeText(this, CalenderHelper.formattedDate(localDate) + " | " + roomName + " clicked", Toast.LENGTH_SHORT).show();
        });
    }


}