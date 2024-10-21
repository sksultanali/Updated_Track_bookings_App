package com.developerali.trackbookings.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.trackbookings.R;
import com.developerali.trackbookings.databinding.ChildRoomCellBinding;

import java.time.LocalDate;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomViewHolder> {

    private final Activity activity;
    private final int totalRooms;
    private final RoomClickListener roomClickListener;
    private LocalDate currentDate;

    // Constructor with RoomClickListener
    public RoomsAdapter(Activity activity, int totalRooms, RoomClickListener roomClickListener) {
        this.activity = activity;
        this.totalRooms = totalRooms;
        this.roomClickListener = roomClickListener;
    }

    // Set the date while binding the view holder in CalendarAdapter
    public void setDate(LocalDate date) {
        this.currentDate = date;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the room item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_room_cell, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        // Bind the room data here (if any)

        // Set click listener for each room item
        holder.binding.roomText.setText("R"+(position+1));
        holder.itemView.setOnClickListener(v -> {
            if (roomClickListener != null) {
                roomClickListener.onRoomClick(holder.binding.roomText.getText().toString(), currentDate);
            }
        });
    }

    @Override
    public int getItemCount() {
        return totalRooms; // Assuming this is the total number of rooms
    }

    // ViewHolder class for Rooms
    public class RoomViewHolder extends RecyclerView.ViewHolder {
        ChildRoomCellBinding binding;
        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ChildRoomCellBinding.bind(itemView);
        }
    }

    // RoomClickListener interface
    public interface RoomClickListener {
        void onRoomClick(String roomName, LocalDate localDate);
    }
}
