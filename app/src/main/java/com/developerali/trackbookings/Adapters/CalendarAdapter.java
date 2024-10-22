package com.developerali.trackbookings.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.trackbookings.CalenderHelper;
import com.developerali.trackbookings.Models.PropertiesModel;
import com.developerali.trackbookings.databinding.CalenderCellBinding;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

import com.developerali.trackbookings.R;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    public final ArrayList<LocalDate> days;
    private final SelectionListner onItemListener;
    private final Activity activity;
    LocalDate today;
    private final PropertiesModel propertiesModel;

    public CalendarAdapter(Activity activity, ArrayList<LocalDate> days, SelectionListner onItemListener, PropertiesModel propertiesModel) {
        this.activity = activity;
        this.days = days;
        this.onItemListener = onItemListener;
        this.propertiesModel = propertiesModel;
        today = LocalDate.now();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calender_cell, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final LocalDate date = days.get(position);

        if (date == null) {
            holder.binding.cellDayText.setText("");
        } else {
            // Display day of the month and day of the week. Above is okay
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            String abbreviatedDayOfWeek = CalenderHelper.getAbbreviatedDayOfWeek(dayOfWeek); // Get abbreviated day
            holder.binding.cellDayText.setText(date.getDayOfMonth() + "\n" + abbreviatedDayOfWeek);

            // Set up the RoomsAdapter with room click listener
            RoomsAdapter roomsAdapter = new RoomsAdapter(activity, propertiesModel, new RoomsAdapter.RoomClickListener() {
                @Override
                public void onRoomClick(String roomName, LocalDate localDate) {
                    // Handle room selection with both room name and date
                    onItemListener.onRoomSelected(roomName, date);
                }
            });

            roomsAdapter.setDate(date);

            if (days.get(position).isBefore(today)) {
                holder.binding.cellDayText.setTextColor(activity.getColor(R.color.defTextCol));
            }

            if (days.get(position).getDayOfWeek().toString().equalsIgnoreCase("sunday")){
                holder.binding.cellDayText.setTextColor(activity.getColor(R.color.red));
            }


            // Setup RecyclerView for rooms
            GridLayoutManager layoutManager = new GridLayoutManager(activity, propertiesModel.getTotalRoom());
            holder.binding.roomRecyclerView.setLayoutManager(layoutManager);
            holder.binding.roomRecyclerView.setAdapter(roomsAdapter);
            roomsAdapter.notifyDataSetChanged();

            // Handle day click
            holder.itemView.setOnClickListener(v -> {
                onItemListener.onShowAction(true);
            });
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CalenderCellBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CalenderCellBinding.bind(itemView);
        }
    }

    // Interface for click events
    public interface SelectionListner {
        void onShowAction(Boolean isSelected);  // For date clicks
        void onRoomSelected(String roomName, LocalDate localDate);  // For room clicks
    }
}
