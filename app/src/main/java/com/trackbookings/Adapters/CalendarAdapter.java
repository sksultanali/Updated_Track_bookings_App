package com.trackbookings.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trackbookings.CalenderHelper;
import com.trackbookings.Models.Booking;
import com.trackbookings.Models.BookingResponse;
import com.trackbookings.databinding.CalenderCellBinding;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trackbookings.R;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    public final ArrayList<LocalDate> days;
    private final SelectionListener onItemListener;
    private final Activity activity;
    private final LocalDate today;
    private final List<BookingResponse.BookingDateData> propertiesModel;
    private final boolean isMultiSelectMode;
    private final Map<LocalDate, ArrayList<String>> selectedRoomsMap = new HashMap<>();
    Animation blankAnimation;

    public CalendarAdapter(Activity activity, ArrayList<LocalDate> days, SelectionListener onItemListener,
                           List<BookingResponse.BookingDateData> propertiesModel, boolean isMultiSelectMode) {
        this.activity = activity;
        this.days = days;
        this.onItemListener = onItemListener;
        this.propertiesModel = propertiesModel;
        this.isMultiSelectMode = isMultiSelectMode;
        blankAnimation = AnimationUtils.loadAnimation(activity, R.anim.blink);
        this.today = LocalDate.now();
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
            holder.binding.roomRecyclerView.setVisibility(View.GONE);
        } else {
            holder.binding.roomRecyclerView.setVisibility(View.VISIBLE);
            // Display day of the month and day of the week
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            String abbreviatedDayOfWeek = CalenderHelper.getAbbreviatedDayOfWeek(dayOfWeek);
            holder.binding.cellDayText.setText(date.getDayOfMonth() + "\n" + abbreviatedDayOfWeek);

            // Highlight today's date
            if (date.equals(today)) {
                holder.binding.cellDayText.setBackgroundResource(R.drawable.bg_blue_purple_corners);
                holder.binding.cellDayText.setTextColor(activity.getColor(R.color.white));
                //holder.binding.cellDayText.startAnimation(blankAnimation);
            } else {
                holder.binding.cellDayText.setBackgroundResource(0);
                holder.binding.cellDayText.setTextColor(activity.getColor(R.color.black));
                //holder.binding.cellDayText.setAnimation(null);
            }

            // Set up the RoomsAdapter with pre-selected rooms
            ArrayList<String> selectedRooms = selectedRoomsMap.getOrDefault(date, new ArrayList<>());
            RoomsAdapter roomsAdapter = new RoomsAdapter(activity, propertiesModel.get(position).getRooms(),
                    new RoomsAdapter.RoomClickListener() {
                @Override
                public void onRoomClick(String roomName, LocalDate localDate, boolean isSelected, Booking booking) {
                    handleRoomSelection(roomName, localDate, isSelected, booking);
                }

                @Override
                public void onRefresh() {
                    onItemListener.onRecordUpdate();
                }
            }, isMultiSelectMode, selectedRooms, date.isBefore(today));

            roomsAdapter.setDate(date);

            // Apply styling for past dates and Sundays
            if (date.isBefore(today)) {
                holder.binding.cellDayText.setTextColor(activity.getColor(R.color.defTextCol));
            }

            if (date.getDayOfWeek().toString().equalsIgnoreCase("SUNDAY")) {
                holder.binding.cellDayText.setTextColor(activity.getColor(R.color.red));
            }

            // Setup RecyclerView for rooms
            GridLayoutManager layoutManager = new GridLayoutManager(activity, propertiesModel.get(position).getRooms().size());
            holder.binding.roomRecyclerView.setLayoutManager(layoutManager);
            holder.binding.roomRecyclerView.setAdapter(roomsAdapter);
        }
    }

    private void handleRoomSelection(String roomName, LocalDate date, boolean isSelected, Booking booking) {
        ArrayList<String> selectedRooms = selectedRoomsMap.getOrDefault(date, new ArrayList<>());

        if (isSelected) {
            if (!selectedRooms.contains(roomName)) {
                selectedRooms.add(roomName);
            }
        } else {
            selectedRooms.remove(roomName);
        }

        selectedRoomsMap.put(date, selectedRooms);
        onItemListener.onSelectionChanged(getTotalSelectedRooms(), getAllSelectedItems(), booking);
    }

    public int getTotalSelectedRooms() {
        int total = 0;
        for (ArrayList<String> rooms : selectedRoomsMap.values()) {
            total += rooms.size();
        }
        return total;
    }

    public Map<LocalDate, ArrayList<String>> getAllSelectedItems() {
        return new HashMap<>(selectedRoomsMap);
    }

    public void clearAllSelections() {
        selectedRoomsMap.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CalenderCellBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CalenderCellBinding.bind(itemView);
        }
    }

    public interface SelectionListener {
        void onSelectionChanged(int totalSelected, Map<LocalDate, ArrayList<String>> selectedItems, Booking booking);
        void onRecordUpdate();
    }
}