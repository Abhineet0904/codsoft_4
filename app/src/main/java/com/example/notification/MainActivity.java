package com.example.notification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputLayout;
import java.io.IOException;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.O)
@SuppressLint("InlinedApi")
public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "Notification_channel";
    private static final String CHANNEL_NAME = "Notification channel";
    private static final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;
    private static final String CHANNEL_DESCRIPTION = "Channel for custom notifications";

    private static final int NOTIFICATION_ID = 1;

    private ImageView iconImage;
    private static final int IMAGE_PICK_REQUEST = 1;
    private Bitmap iconBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1
            );
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1
            );
        }
        createNotificationChannel();


        iconImage = findViewById(R.id.icon);
        iconImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, "Click to select notification image", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        iconImage.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                Intent imagePicker = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePicker.setType("image/*");
                startActivityForResult(imagePicker, IMAGE_PICK_REQUEST);
            }
        });


        TextInputLayout titleLayout = findViewById(R.id.title);


        TextInputLayout messageLayout = findViewById(R.id.message);


        Spinner fontSpinner = findViewById(R.id.font);
        String[] fontList = {"Select notification style", "Bold", "Italics", "Bold & Italics"};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, R.layout.spinner_closed_layout, fontList);
        adapter1.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        fontSpinner.setAdapter(adapter1);


        Spinner colourSpinner = findViewById(R.id.colour);
        String[] colourList = {"Select notification colour", "Red", "Yellow", "Blue", "Cyan", "Gray", "Green", "Magenta", "Transparent"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, R.layout.spinner_closed_layout, colourList);
        adapter2.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        colourSpinner.setAdapter(adapter2);


        Button show = findViewById(R.id.button);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = String.valueOf(Objects.requireNonNull(titleLayout.getEditText()).getText());
                String message = String.valueOf(Objects.requireNonNull(messageLayout.getEditText()).getText());
                String font = fontSpinner.getSelectedItem().toString();
                String colour = colourSpinner.getSelectedItem().toString();
                Bitmap icon = iconBitmap;


                if (colour.equals("Select notification colour"))
                    colour = "Black";
                else if (colour.equals("Transparent"))
                    colour = "White";


                if (title.isEmpty())
                    titleLayout.getEditText().setError("Enter notification title");
                else if (message.isEmpty())
                    messageLayout.getEditText().setError("Enter notification title");


                if (!title.isEmpty() && !message.isEmpty())
                {
                    displayNotification(title, message, font, colour, icon);
                }
            }
        });
    }




    private void createNotificationChannel() {

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, CHANNEL_IMPORTANCE);
        channel.setDescription(CHANNEL_DESCRIPTION);
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        channel.enableVibration(true);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();
            if (imageUri != null)
            {
                try
                {
                    //iconImage.setImageURI(imageUri);
                    iconBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    iconImage.setImageBitmap(iconBitmap);
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
                }
                catch (IOException e)
                {
                    Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show();;
                }
            }
            else
            {
                Toast.makeText(this, "Invalid image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }




    private void displayNotification(String title, String message, String font, String colour, Bitmap icon) {
        try
        {
            int style = Typeface.NORMAL;

            if (Objects.equals(font, "Bold"))
                style = Typeface.BOLD;
            else if (Objects.equals(font, "Italics"))
                style = Typeface.ITALIC;
            else if (Objects.equals(font, "Bold & Italics"))
                style = Typeface.BOLD_ITALIC;


            int iconColor = Color.parseColor(colour.toUpperCase());


            Spannable iconTitle = new SpannableString(title);
            iconTitle.setSpan(new ForegroundColorSpan(iconColor), 0, iconTitle.length(), 0);
            iconTitle.setSpan(new StyleSpan(style), 0, iconTitle.length(), 0);

            Spannable iconMessage = new SpannableString(message);
            iconMessage.setSpan(new ForegroundColorSpan(iconColor), 0, iconMessage.length(), 0);
            iconMessage.setSpan(new StyleSpan(style), 0, iconMessage.length(), 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_notifications_none_24)
                    .setLargeIcon(icon)
                    .setContentTitle(iconTitle)
                    .setContentText(iconMessage)
                    .setColor(iconColor)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null)
            {
                notificationManager.notify(NOTIFICATION_ID, notification);
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}