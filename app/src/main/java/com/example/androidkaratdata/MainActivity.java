package com.example.androidkaratdata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidkaratdata.models.DeviceQuery;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import static com.example.androidkaratdata.SettingActivity.APP_PREFERENCES;


public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText editText_name;
    TextView textView_device, textView_connectionSettings;
    Spinner spinnerDevice;
    CalendarView calendarView;
    TextView textView;
    ImageButton imageButtonSetting, openBtn, editConnection;
    Button buttonRead;
    DeviceQuery query;
    SharedPreferences mSettings;


    String port, ip, adr, mode;
    int cYear, cMonth, cDay;
    Date start = new Date();
    int dialogCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.myToolBar);
        textView_device = findViewById(R.id.tView_device);
        textView = findViewById(R.id.textView_date);
        textView_connectionSettings = findViewById(R.id.connection_settings);
        editText_name = findViewById(R.id.editText_name);
        spinnerDevice = findViewById(R.id.spinner_device);
        editConnection = findViewById(R.id.imageButton_connection_setting);



        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.spinner_array, R.layout.spinner_item);
        spinnerDevice.setAdapter(adapter);
        setSupportActionBar(toolbar);

        dialogCounter = 0;

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        textView_connectionSettings.setText(getConnectionSettings());

        calendarView = findViewById(R.id.calendarView);
        start.setTime(calendarView.getDate());
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year,
                                            int month, int dayOfMonth) {
                cYear = year + 100;
                cMonth = month;
                cDay = dayOfMonth;
                String selectedDate = new StringBuilder().append(cMonth + 1)
                        .append("-").append(cDay).append("-").append(cYear - 100)
                        .append(" ").toString();
                textView.setText("Начать с " + selectedDate);
            }
        });

        //обработчик кнопки "настройки"
        imageButtonSetting = findViewById(R.id.imageButton_setting);
        imageButtonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        editConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        buttonRead = findViewById(R.id.button_read);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cDay != 0)
                    start = new Date(cYear - 2000, cMonth, cDay);
                if (mode != null) {
                    if (mode.equals("TCP")) {
                        query = new DeviceQuery(
                                port, ip, adr, start,
                                getArchivesTypes(), editText_name.getText().toString()
                        );
                        if (port == null || ip == null || adr == null)
                            Toast.makeText(getApplicationContext(), "Определите параметры соединения в настройках (⚙)", Toast.LENGTH_LONG).show();
                        else if (getArchivesTypes().size() == 0)
                            Toast.makeText(getApplicationContext(), "Выберите хотя бы один архив", Toast.LENGTH_LONG).show();
                        else showDialog(dialogCounter++);
                    } else
                        Toast.makeText(getApplicationContext(), "Подключение по USB в разработке", Toast.LENGTH_LONG).show();
                } else Toast.makeText(getApplicationContext(), "Определите параметры соединения в настройках (⚙)", Toast.LENGTH_LONG).show();
            }
        });

        openBtn = (ImageButton) findViewById(R.id.imageButton_open);
        openBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getExternalFilesDir("Karat");
                Uri uri = Uri.parse(directory.toString());
                Log.d("Uri", uri.getPath());
                intent.setDataAndType(uri, "*/*");
                try {
                    startActivity(Intent.createChooser(intent, "Выберите менеджер файлов (проводник)"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "Установите один из менеджеров файлов.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getConnectionSettings() {
        if(mSettings.contains("IP")) ip = (mSettings.getString("IP", ""));
        if(mSettings.contains("Port")) port = (mSettings.getString("Port", ""));
        if(mSettings.contains("Adr")) adr = (mSettings.getString("Adr", ""));
        if(mSettings.contains("Mode")) mode = (mSettings.getBoolean("Mode", true))?"TCP":"USB";
        return mode + " " + ip + ":" + port + "/" + adr;
    }

    private ArrayList<String> getArchivesTypes() {
        ArrayList<String> res = new ArrayList<>();
        CheckBox h = findViewById(R.id.hour);
        CheckBox d = findViewById(R.id.day);
        CheckBox m = findViewById(R.id.month);
        CheckBox emer = findViewById(R.id.alarm);
        CheckBox integ = findViewById(R.id.integral);
        CheckBox prot = findViewById(R.id.protected_journal);
        CheckBox event = findViewById(R.id.event);
        if (h.isChecked())
            res.add(getString(R.string.hourly));
        if (d.isChecked())
            res.add(getString(R.string.daily));
        if (m.isChecked())
            res.add(getString(R.string.monthly));
        if (emer.isChecked())
            res.add(getString(R.string.emergency));
        if (integ.isChecked())
            res.add(getString(R.string.integral));
        if (prot.isChecked())
            res.add(getString(R.string.protective));
        if (event.isChecked())
            res.add(getString(R.string.eventful));
        return res;
    }

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        // заголовок
        adb.setTitle("Подтвердите запрос");
        // сообщение
        adb.setMessage(query.toString());
        // иконка
        adb.setIcon(android.R.drawable.ic_dialog_info);
        // кнопка положительного ответа
        adb.setPositiveButton("Да", myClickListener);
        // кнопка отрицательного ответа
        adb.setNegativeButton("Нет", myClickListener);
        // создаем диалог
        return adb.create();
    }

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    Intent toTerm = new Intent(MainActivity.this, TCPTerminalActivity.class);
                    toTerm.putExtra("query", query);
                    if (editText_name.getText().toString() != null)
                        toTerm.putExtra("fname", editText_name.getText().toString()
                                .replaceAll("[^\\da-zA-Zа-яёА-ЯЁ]", ""));
                    startActivity(toTerm);
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    Toast.makeText(getApplicationContext(),
                            "Исправьте поля", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
