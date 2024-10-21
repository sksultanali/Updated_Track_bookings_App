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

import java.time.LocalDate;
import java.util.ArrayList;
import com.developerali.trackbookings.R;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    public final ArrayList<LocalDate> days;
    private final SelectionListner onItemListener;
    private final Activity activity;
    private final PropertiesModel propertiesModel;

    public CalendarAdapter(Activity activity, ArrayList<LocalDate> days, SelectionListner onItemListener, PropertiesModel propertiesModel) {
        this.activity = activity;
        this.days = days;
        this.onItemListener = onItemListener;
        this.propertiesModel = propertiesModel;
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
            // Display day of the month and day of the week
            String dayOfWeek = date.getDayOfWeek().toString().toLowerCase();
            holder.binding.cellDayText.setText(date.getDayOfMonth() + "\n" + dayOfWeek);

            // Set up the RoomsAdapter with room click listener
            RoomsAdapter roomsAdapter = new RoomsAdapter(activity, propertiesModel.getTotalRoom(), new RoomsAdapter.RoomClickListener() {
                @Override
                public void onRoomClick(String roomName, LocalDate localDate) {
                    // Handle room selection with both room name and date
                    onItemListener.onRoomSelected(roomName, date);
                }
            });

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
