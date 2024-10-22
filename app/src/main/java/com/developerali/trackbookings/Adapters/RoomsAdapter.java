package com.developerali.trackbookings.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.trackbookings.CalenderHelper;
import com.developerali.trackbookings.Models.PropertiesModel;
import com.developerali.trackbookings.R;
import com.developerali.trackbookings.databinding.ChildRoomCellBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomViewHolder> {

    private final Activity activity;
    PropertiesModel propertiesModel;
    private final RoomClickListener roomClickListener;
    public LocalDate currentDate;
    FirebaseDatabase database;
    LocalDate today;

    // Constructor with RoomClickListener
    public RoomsAdapter(Activity activity, PropertiesModel propertiesModel, RoomClickListener roomClickListener) {
        this.activity = activity;
        this.propertiesModel = propertiesModel;
        this.roomClickListener = roomClickListener;
        database = FirebaseDatabase.getInstance();
        today = LocalDate.now();
    }

    // Set the date while binding the view holder in CalendarAdapter
    public void setDate(LocalDate date) {
        this.currentDate = date; // Correctly update the date for each instance
        notifyDataSetChanged();  // Refresh adapter when the date changes
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the room item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_room_cell, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Bind the room data here (if any)

        // Set click listener for each room item
        holder.binding.roomText.setText(""+(position+1));
        holder.itemView.setOnClickListener(v -> {
            if (roomClickListener != null) {
                roomClickListener.onRoomClick(holder.binding.roomText.getText().toString(), currentDate);
            }
        });

        database.getReference().child("booking")
                .child(propertiesModel.getName())
                .child(CalenderHelper.dateKey(currentDate))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.child(""+(position+1)).exists()){
                                boolean booked = snapshot.child(""+(position+1)).getValue(Boolean.class);
                                if (booked){
                                    holder.binding.roomText.setBackground(activity.getDrawable(R.drawable.bg_light_red_corners));
                                }else {
                                    if (currentDate.getDayOfWeek().toString().equalsIgnoreCase("sunday")){
                                        holder.binding.roomText.setTextColor(activity.getColor(R.color.red));
                                    }else if (currentDate.isBefore(today)) {
                                        //holder.binding.roomText.setText("-");
                                        holder.binding.roomText.setTextColor(activity.getColor(R.color.defTextCol));
                                    }
                                    holder.binding.roomText.setBackground(activity.getDrawable(R.drawable.bg_black_border));
                                }
                            }else {
                                if (currentDate.getDayOfWeek().toString().equalsIgnoreCase("sunday")){
                                    holder.binding.roomText.setTextColor(activity.getColor(R.color.red));
                                }else if (currentDate.isBefore(today)) {
                                    //holder.binding.roomText.setText("-");
                                    holder.binding.roomText.setTextColor(activity.getColor(R.color.defTextCol));
                                }
                                holder.binding.roomText.setBackground(activity.getDrawable(R.drawable.bg_black_border));
                            }
                        }else {
                            if (currentDate.getDayOfWeek().toString().equalsIgnoreCase("sunday")){
                                holder.binding.roomText.setTextColor(activity.getColor(R.color.red));
                            }else if (currentDate.isBefore(today)) {
                                //holder.binding.roomText.setText("-");
                                holder.binding.roomText.setTextColor(activity.getColor(R.color.defTextCol));
                            }
                            holder.binding.roomText.setBackground(activity.getDrawable(R.drawable.bg_black_border));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (currentDate.getDayOfWeek().toString().equalsIgnoreCase("sunday")){
            holder.binding.roomText.setTextColor(activity.getColor(R.color.red));
        }


    }

    @Override
    public int getItemCount() {
        return propertiesModel.getTotalRoom(); // Assuming this is the total number of rooms
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
