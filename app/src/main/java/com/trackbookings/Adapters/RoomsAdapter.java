package com.trackbookings.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.trackbookings.Helpers.ApiService;
import com.trackbookings.Helpers.Helpers;
import com.trackbookings.Helpers.RetrofitClient;
import com.trackbookings.Models.ApiResponse;
import com.trackbookings.Models.Booking;
import com.trackbookings.Models.BookingResponse;
import com.trackbookings.R;
import com.trackbookings.databinding.ChildRoomCellBinding;
import com.trackbookings.databinding.DailogAddDataBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomViewHolder> {

    private final Activity activity;
    private final List<BookingResponse.Room> arrayList;
    private final RoomClickListener roomClickListener;
    private final boolean isMultiSelectMode;
    private final boolean isPastDate;
    private LocalDate currentDate;
    private final FirebaseDatabase database;
    private final Set<Integer> selectedPositions = new HashSet<>();
    private final ArrayList<String> preSelectedRooms;
    Animation blankAnimation;
    ApiService apiService;
    ProgressDialog progressDialog;

    public RoomsAdapter(Activity activity, List<BookingResponse.Room> arrayList,
                        RoomClickListener roomClickListener, boolean isMultiSelectMode,
                        ArrayList<String> preSelectedRooms, boolean isPastDate) {
        this.activity = activity;
        this.arrayList = arrayList;
        this.roomClickListener = roomClickListener;
        this.isMultiSelectMode = isMultiSelectMode;
        this.preSelectedRooms = preSelectedRooms;
        this.isPastDate = isPastDate;
        blankAnimation = AnimationUtils.loadAnimation(activity, R.anim.blink);
        apiService = RetrofitClient.getClient().create(ApiService.class);
        this.database = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Connecting Server...");
        progressDialog.setCancelable(false);
    }

    public void setDate(LocalDate date) {
        this.currentDate = date;
        loadPreSelectedRooms();
        notifyDataSetChanged();
    }

    private void loadPreSelectedRooms() {
        selectedPositions.clear();
        if (preSelectedRooms != null) {
            for (String room : preSelectedRooms) {
                try {
                    int roomNum = Integer.parseInt(room) - 1;
                    if (roomNum >= 0 && roomNum < arrayList.size()) {
                        selectedPositions.add(roomNum);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.child_room_cell, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String roomNumber = String.valueOf(position + 1);
        BookingResponse.Room room = arrayList.get(position);

        holder.binding.roomText.setText(roomNumber);

        // Check if room is selected
        if (isPastDate) {
            holder.itemView.setEnabled(false);
            holder.itemView.setAlpha(0.6f);
            resetRoomAppearanceDisable(holder);
        } else {
            holder.itemView.setEnabled(true);
            holder.itemView.setAlpha(1f);
            resetRoomAppearanceActive(holder);
        }

        boolean isSelected = selectedPositions.contains(position);
        if (isSelected) {
            holder.binding.mainLayout.setBackground(
                    ContextCompat.getDrawable(activity, R.drawable.bg_selected));
        }

        holder.itemView.setOnClickListener(v -> {
            if (Helpers.getTextFromSharedPref(activity, "access_type") == null ||
                    Helpers.getTextFromSharedPref(activity, "access_type").equalsIgnoreCase("viewer")) {
                if (room.getBooking() == null){
                    Helpers.showOnlyMessage(activity, "No Booking",
                            "This date has <b>no booking</b> or editor didn't edited this record!");
                }else {
                    showDetailsDialog(room);
                }
            }else {
                if (currentDate == null || isPastDate) return;
                if (isMultiSelectMode) {
                    toggleSelection(position);
                    boolean newSelectedState = selectedPositions.contains(position);
                    roomClickListener.onRoomClick(roomNumber, currentDate, newSelectedState, room.getBooking());

                    // Update appearance
                    if (newSelectedState) {
                        holder.binding.mainLayout.setBackground(
                                ContextCompat.getDrawable(activity, R.drawable.bg_selected));
                    } else {
                        resetRoomAppearanceActive(holder);
                        checkBookingStatus(holder, position, room);
                    }
                } else {
                    // Single selection mode
                    if (selectedPositions.contains(position)) {
                        selectedPositions.clear();
                        resetRoomAppearanceActive(holder);
                        roomClickListener.onRoomClick(roomNumber, currentDate, false, room.getBooking());
                        checkBookingStatus(holder, position, room);
                    } else {
                        selectedPositions.clear();
                        selectedPositions.add(position);
                        notifyDataSetChanged();
                        roomClickListener.onRoomClick(roomNumber, currentDate, true, room.getBooking());
                    }
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (room.getBooking() == null){
                    Helpers.showOnlyMessage(activity, "No Booking",
                            "This date has <b>no booking</d> or editor didn't edited this record!");
                }else {
                    showDetailsDialog(room);
                }
                return true;
            }
        });

        checkBookingStatus(holder, position, room);


    }

    void showDetailsDialog(BookingResponse.Room room){
        DailogAddDataBinding dialogBinding = DailogAddDataBinding.inflate(activity.getLayoutInflater());
        // Create a new dialog and set the custom layout
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(dialogBinding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setStatusBarColor(activity.getColor(R.color.white));
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        dialogBinding.closeBtn.setOnClickListener(v->{
            dialog.dismiss();
        });

        dialogBinding.idName.setText("By " + room.getBooking().getUser_name());
        if (room.getBooking().getPhone() != null){
            dialogBinding.whatsappBtn.setVisibility(View.VISIBLE);
            dialogBinding.bookingPhone.setText(room.getBooking().getPhone());
            dialogBinding.whatsappBtn.setOnClickListener(v->{
                Helpers.openLink(activity, "https://wa.me/+91"+room.getBooking().getPhone());
            });
        }else {
            dialogBinding.bookingPhone.setText("NA");
            dialogBinding.whatsappBtn.setVisibility(View.GONE);
        }
        dialogBinding.dateTime.setText(
                Helpers.formatDate(room.getBooking().getDate(), "yyyyMMdd", "dd LLL, yyyy")
                        + " | Room No. "
                        + room.getBooking().getRoom_no()
        );

        if (Helpers.getTextFromSharedPref(activity, "access_type") == null ||
                Helpers.getTextFromSharedPref(activity, "access_type").equalsIgnoreCase("viewer")) {
            dialogBinding.deleteBtn.setVisibility(View.GONE);
            dialogBinding.saveBtn.setVisibility(View.GONE);
        }else {
            dialogBinding.deleteBtn.setVisibility(View.VISIBLE);
            dialogBinding.saveBtn.setVisibility(View.VISIBLE);
        }


        String bName = room.getBooking().getBooking_name() == null ? "" : room.getBooking().getBooking_name();
        String bRemark = room.getBooking().getRemark() == null ? "" : room.getBooking().getRemark();
        String bPax = room.getBooking().getPax() == null ? "NA" : room.getBooking().getPax();
        String bDue = room.getBooking().getDue() == null ? "NA" : room.getBooking().getDue();

        dialogBinding.bookingName.setText(bName);
        dialogBinding.remarks.setText(bRemark);
        dialogBinding.pax.setText(bPax);
        dialogBinding.due.setText(bDue);

        dialogBinding.deleteBtn.setVisibility(View.GONE);
        dialogBinding.deleteBtn.setOnClickListener(v->{
            Helpers.showActionDialog(activity, "Delete?", "Are you sure want to delete this record?", "Delete",
                    null, true, new Helpers.DialogButtonClickListener() {
                        @Override
                        public void onYesButtonClicked() {
                            Call<ApiResponse> call = apiService.deleteTableRecord("bookings", room.getBooking().getId());
                            dialog.dismiss();
                            executeCall(call);
                        }

                        @Override
                        public void onNoButtonClicked() {

                        }

                        @Override
                        public void onCloseButtonClicked() {

                        }
                    });
        });

        dialogBinding.saveBtn.setText("Update");
        dialogBinding.bookingName.setEnabled(false);
        dialogBinding.due.setEnabled(false);
        dialogBinding.pax.setEnabled(false);
        dialogBinding.bookingPhone.setEnabled(false);

        dialogBinding.bookingName.setTextColor(activity.getColor(R.color.icon_color));
        dialogBinding.pax.setTextColor(activity.getColor(R.color.icon_color));
        dialogBinding.due.setTextColor(activity.getColor(R.color.icon_color));
        dialogBinding.bookingPhone.setTextColor(activity.getColor(R.color.icon_color));

        dialogBinding.pax.setBackground(activity.getDrawable(R.drawable.bg_gray_round_corner));
        dialogBinding.due.setBackground(activity.getDrawable(R.drawable.bg_gray_round_corner));
        dialogBinding.bookingName.setBackground(activity.getDrawable(R.drawable.bg_gray_round_corner));
        dialogBinding.bookingPhone.setBackground(activity.getDrawable(R.drawable.bg_gray_round_corner));

        dialogBinding.saveBtn.setOnClickListener(v->{
            String remark = dialogBinding.remarks.getText().toString();
            Call<ApiResponse> call = apiService.editTable("bookings",
                    "remark", remark, room.getBooking().getId());
            dialog.dismiss();
            executeCall(call);
        });

        dialog.show();
    }

    private void checkBookingStatus(RoomViewHolder holder, int position, BookingResponse.Room room) {
        if (room.getBooking() != null){
            String name = room.getBooking().getBooking_name() == null ? "-" :
                    room.getBooking().getBooking_name().equalsIgnoreCase("PencilBooking") ? "-" :
                            room.getBooking().getBooking_name();
            holder.binding.name.setText(name);

            if (room.getBooking().getBooking_name()!= null &&
            room.getBooking().getBooking_name().equalsIgnoreCase("PencilBooking")){
                holder.binding.mainLayout.setBackground(activity.getDrawable(R.drawable.bg_pencil_booking));
            }else {
                if (room.getBooking().getUser_color_code().equalsIgnoreCase("blue")) {
                    holder.binding.mainLayout.setBackground(activity.getDrawable(R.drawable.bg_blue_purple_corners));
                } else if (room.getBooking().getUser_color_code().equalsIgnoreCase("green")) {
                    holder.binding.mainLayout.setBackground(activity.getDrawable(R.drawable.bg_green_corner));
                } else if (room.getBooking().getUser_color_code().equalsIgnoreCase("red")) {
                    holder.binding.mainLayout.setBackground(activity.getDrawable(R.drawable.bg_red_corners));
                } else {
                    holder.binding.mainLayout.setBackground(Helpers.createBorderedDrawable(activity,
                            Color.parseColor(room.getBooking().getUser_color_code()), 0));
                    if (Helpers.isDarkColor(room.getBooking().getUser_color_code())){
                        holder.binding.roomText.setTextColor(activity.getColor(R.color.white));
                        holder.binding.name.setTextColor(activity.getColor(R.color.yellow));
                    }else {
                        holder.binding.roomText.setTextColor(activity.getColor(R.color.black));
                        holder.binding.name.setTextColor(activity.getColor(R.color.blue_purple));
                    }
                }
            }

            if (room.getBooking().getRemark() != null && !room.getBooking().getRemark().isEmpty()){
                if (Helpers.isDarkColor(room.getBooking().getUser_color_code())){
                    holder.binding.remarkIcon.setColorFilter(ContextCompat.getColor(activity, R.color.white));
                }else {
                    holder.binding.remarkIcon.setColorFilter(ContextCompat.getColor(activity, R.color.black));
                }

                holder.binding.remarkIcon.setVisibility(View.VISIBLE);
                holder.binding.remarkIcon.startAnimation(blankAnimation);
            }else {
                holder.binding.remarkIcon.setVisibility(View.GONE);
            }

        }else {
            holder.binding.remarkIcon.setVisibility(View.GONE);
        }
    }

    private void executeCall(Call<ApiResponse> call) {
        progressDialog.show();
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    ApiResponse apiResponse = response.body();
                    progressDialog.dismiss();
                    roomClickListener.onRefresh();
                    if (!apiResponse.getStatus().equalsIgnoreCase("success")){
                        Toast.makeText(activity, apiResponse.getStatus() +
                                ": " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(activity, response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(activity, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
    }

    private void resetRoomAppearanceActive(RoomViewHolder holder) {
        holder.binding.mainLayout.setBackground(
                ContextCompat.getDrawable(activity, R.drawable.bg_black_border));
        holder.binding.roomText.setTextColor(
                ContextCompat.getColor(activity, R.color.black));
    }

    private void resetRoomAppearanceDisable(RoomViewHolder holder) {
        holder.binding.mainLayout.setBackground(
                ContextCompat.getDrawable(activity, R.drawable.bg_grey_border));
        holder.binding.roomText.setTextColor(
                ContextCompat.getColor(activity, R.color.darkGray));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        ChildRoomCellBinding binding;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ChildRoomCellBinding.bind(itemView);
        }
    }

    public interface RoomClickListener {
        void onRoomClick(String roomName, LocalDate localDate, boolean isSelected, Booking booking);
        void onRefresh();
    }
}