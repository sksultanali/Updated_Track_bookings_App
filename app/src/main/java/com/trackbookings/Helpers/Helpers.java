package com.trackbookings.Helpers;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.trackbookings.databinding.CustomDialogBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Helpers {

    public static String DESCRIPTION;
    public static int DISCOUNT = 0;
    public static String START_DATE;
    public static String END_DATE;

    public static void plusBtn(Context context, TextView textView){
        String no = textView.getText().toString();
        int number = Integer.parseInt(no);

        if (number < 10){
            int newNum = number + 1 ;
            textView.setText(String.valueOf(newNum));
        }else {
            Toast.makeText(context, "value exceed", Toast.LENGTH_LONG).show();
        }
    }

    public static GradientDrawable createBorderedDrawable(Activity activity, int fillColor, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(fillColor);
        if (strokeColor != 0) {
            drawable.setStroke(1, strokeColor);
        }
        float radiusDp = 8;
        float radiusPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, radiusDp, activity.getResources().getDisplayMetrics()
        );
        drawable.setCornerRadius(radiusPx);
        return drawable;
    }

    public static boolean isDarkColor(String colorCode) {
        try {
            int color = Color.parseColor(colorCode);  // Convert string to color int
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            // Calculate luminance using a standard formula
            double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;

            return luminance < 0.5; // true = dark, false = light
        } catch (IllegalArgumentException e) {
            // Invalid color string
            e.printStackTrace();
            return false;
        }
    }


    public static void setStatusBarTheme(Activity activity, int statusBarColor, boolean isDarkIcons) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();

            // Set status bar color
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(activity, statusBarColor));

            // Set status bar icons to light/dark
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                if (isDarkIcons) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // Dark icons
                } else {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // Light icons
                }
                decorView.setSystemUiVisibility(flags);
            }
        }
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static String formatDate(String inputTime, String inputFormat, String outputFormat) {
        try {
            // Parse and format date
            SimpleDateFormat inputFormatter = new SimpleDateFormat(inputFormat);
            SimpleDateFormat outputFormatter = new SimpleDateFormat(outputFormat);
            Date date = inputFormatter.parse(inputTime);
            return outputFormatter.format(date);
        } catch (Exception e) {
            // Return a default value in case of failure
            return inputTime;
        }
    }

    public static String formatLocalDateToYYYYMMDD(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return date.format(formatter);
    }

    public static String convertDateFormat(String inputDate) {
        try {
            // Define input and output date formats
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

            // Parse input date string
            Date date = inputFormat.parse(inputDate);

            // Format and return the new date string
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid Date";
        }
    }

    public static void minusBtn(Context context, TextView textView){
        String no = textView.getText().toString();
        int number = Integer.parseInt(no);

        if (number > 0){
            int newNum = number - 1 ;
            textView.setText(String.valueOf(newNum));
        }else {
            Toast.makeText(context, "value exceed", Toast.LENGTH_LONG).show();
        }
    }

    public static String getLastPartAfterSplit(String input, String delimiter) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String[] parts = input.split(": ");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        } else {
            return "";
        }
    }

    public static void saveTextToSharedPref(Activity activity, String key, String value) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public static String getTextFromSharedPref(Activity activity, String key) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }


    public static String capitalizeSentences(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] sentences = input.split("(?<=[.!?])\\s*"); // Split sentences using punctuation marks
        StringBuilder result = new StringBuilder();

        for (String sentence : sentences) {
            if (!sentence.isEmpty()) {
                String[] words = sentence.split("\\s+");
                StringBuilder capitalizedSentence = new StringBuilder();

                for (String word : words) {
                    if (!word.isEmpty()) {
                        capitalizedSentence.append(Character.toUpperCase(word.charAt(0)))
                                .append(word.substring(1)) // Append the rest of the word
                                .append(" "); // Add a space after each word
                    }
                }
                result.append(capitalizedSentence.toString().trim())
                        .append(" ");
            }
        }

        return result.toString().trim(); // Remove any extra spaces at the end
    }


    public static void setCapitalizedTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;
            private int cursorPosition = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cursorPosition = editText.getSelectionStart();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;

                isEditing = true;
                String originalText = s.toString();
                String formattedText = capitalizeEachWord(originalText);

                if (!originalText.equals(formattedText)) {
                    editText.setText(formattedText);
                    editText.setSelection(Math.min(cursorPosition, formattedText.length())); // Restore cursor position
                }
                isEditing = false;
            }
        });
    }

    private static String capitalizeEachWord(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] words = input.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
            }
            result.append(" ");
        }
        return result.toString().trim();
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static void showOnlyMessage(Activity activity, String title, String content){
        Helpers.showActionDialog(activity, capitalizeFirstLetter(title), content, "Okay", null, true,
                new DialogButtonClickListener() {
                    @Override
                    public void onYesButtonClicked() {}
                    @Override
                    public void onNoButtonClicked() {}
                    @Override
                    public void onCloseButtonClicked() {}
                });
    }

    public interface DialogButtonClickListener {
        void onYesButtonClicked();
        void onNoButtonClicked();
        void onCloseButtonClicked();
    }

    @SuppressLint("ResourceAsColor")
    public static void showActionDialog(
            Activity activity, String title,
            String content, String yesBtn, String noBtn, boolean closeBtn,
            DialogButtonClickListener listener) { // Add listener parameter
        CustomDialogBinding dialogBinding = CustomDialogBinding.inflate(LayoutInflater.from(activity));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(dialogBinding.getRoot())
                .create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        dialogBinding.titleText.setText(title);
        dialogBinding.messageText.setText(Html.fromHtml(content));
        if (yesBtn != null) {
            dialogBinding.loginBtn.setVisibility(View.VISIBLE);
            dialogBinding.yesBtnText.setText(yesBtn);
        }else {
            dialogBinding.loginBtn.setVisibility(View.GONE);
        }

        if (noBtn != null) {
            dialogBinding.noBtn.setVisibility(View.VISIBLE);
            dialogBinding.noBtnText.setText(noBtn);
        }else {
            dialogBinding.noBtn.setVisibility(View.GONE);
        }

        if (closeBtn){
            dialogBinding.closeBtn.setVisibility(View.VISIBLE);
        }else {
            dialogBinding.closeBtn.setVisibility(View.GONE);
        }

        dialogBinding.noBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoButtonClicked();
            }
            dialog.dismiss();
        });

        dialogBinding.loginBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onYesButtonClicked();
            }
            dialog.dismiss();
        });

        dialogBinding.closeBtn.setOnClickListener(v->{
            if (listener != null) {
                listener.onCloseButtonClicked();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

}
