package com.skye.skyetracker;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.TimeZone;

/**
 * Created by Me on 9/30/2015.
 */
public class SetupTab extends Fragment {

    Button btnSyncTime, btnSetLimits;
    CheckBox dualAxis;
    ConfigTransfer configTransfer;
    NumberPicker npEast, npWest, npMinElevation, npMaxElevation, horizontalLength, verticalLength, horizontalSpeed, verticalSpeed;
    LocationManager locationManager;
    TextView lat, lon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        configTransfer = new ConfigTransfer();
        View rootView = inflater.inflate(R.layout.setup, container, false);
        btnSyncTime = (Button) rootView.findViewById(R.id.btnSyncTime);
        btnSetLimits = (Button) rootView.findViewById(R.id.btnSetLimits);
        locationManager = (LocationManager)MainApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        final String[] lengths = container.getContext().getResources().getStringArray(R.array.actuator_length_spinner_item);

        horizontalLength = (NumberPicker) rootView.findViewById(R.id.horizontalActuatorLength);
        horizontalLength.setDisplayedValues(lengths);
        horizontalLength.setMinValue(0);
        horizontalLength.setMaxValue(5);
        horizontalLength.setWrapSelectorWheel(true);
        horizontalLength.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        horizontalLength.setValue(2);
        horizontalLength.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                configTransfer.lh = newVal;
            }
        });
        verticalLength = (NumberPicker) rootView.findViewById(R.id.verticalActuatorLength);
        verticalLength.setDisplayedValues(lengths);
        verticalLength.setMinValue(0);
        verticalLength.setMaxValue(5);
        verticalLength.setWrapSelectorWheel(true);
        verticalLength.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        verticalLength.setValue(1);
        verticalLength.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                configTransfer.lv = newVal;
            }
        });
        horizontalSpeed = (NumberPicker) rootView.findViewById(R.id.horizontalActuatorSpeed);
        horizontalSpeed.setMinValue(20);
        horizontalSpeed.setMaxValue(80);
        horizontalSpeed.setWrapSelectorWheel(true);
        horizontalSpeed.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        horizontalSpeed.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                double val = i / 100.0;
                return String.format("%.2f", val);
            }
        });
        horizontalSpeed.setValue(31);
        horizontalSpeed.invalidate();
        horizontalSpeed.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                configTransfer.sh = newVal;
            }
        });
        verticalSpeed = (NumberPicker) rootView.findViewById(R.id.verticalActuatorSpeed);
        verticalSpeed.setMinValue(20);
        verticalSpeed.setMaxValue(80);
        verticalSpeed.setWrapSelectorWheel(true);
        verticalSpeed.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        verticalSpeed.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                double val = i / 100.0;
                return String.format("%.2f", val);
            }
        });
        verticalSpeed.setValue(31);
        verticalSpeed.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                configTransfer.sv = newVal;
            }
        });
        // Android bug workaround
        try {
            Method method = verticalSpeed.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
            method.setAccessible(true);
            method.invoke(verticalSpeed, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Method method = horizontalSpeed.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
            method.setAccessible(true);
            method.invoke(horizontalSpeed, true);
        } catch (Exception e) {
            e.printStackTrace();
        }



        long current_time = System.currentTimeMillis();
        TimeZone tz = TimeZone.getDefault();
        int tzOffset = tz.getOffset(current_time);
        tzOffset /= (60*60*1000); // in hours

        configTransfer.u = tzOffset;

        LocalBroadcastManager.getInstance(container.getContext()).registerReceiver(mConfigurationReceiver, new IntentFilter("com.skye.skyetracker.configuration"));
        lat = (TextView)rootView.findViewById(R.id.textLatitude);
        lon = (TextView)rootView.findViewById(R.id.textLongitude);
        dualAxis = (CheckBox)rootView.findViewById(R.id.checkbox_dualAxis);
        dualAxis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                configTransfer.d = isChecked;
            }
        });

        btnSyncTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                long current_time = System.currentTimeMillis();
                TimeZone tz = TimeZone.getDefault();
                int tzOffset = tz.getOffset(current_time);
                MainApplication.SendCommand(String.format("SetDateTime|%d", (current_time + tzOffset) / 1000)); // add utc offset and divide by 1000 to get local time in unixtime seconds
            }
        });

        btnSetLimits.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    configTransfer.o = (float)(location.getLongitude());
                    configTransfer.a = (float)location.getLatitude();
                }
                else
                {
                    Toast.makeText(MainApplication.getAppContext(), "Could not get devices GPS location ", Toast.LENGTH_LONG).show();
                }
                Gson gson = new Gson();
                ConfigLocation configLocation = new ConfigLocation(configTransfer);
                Limits limits = new Limits(configTransfer);
                Actuator actuator = new Actuator(configTransfer);
                ConfigOptions configOptions = new ConfigOptions(configTransfer);
                String json = "SetC|" + gson.toJson(configLocation);
                MainApplication.SendCommand(json);
                json = "SetA|" + gson.toJson(actuator);
                MainApplication.SendCommand(json);
                json = "SetL|" + gson.toJson(limits);
                MainApplication.SendCommand(json);
                json = "SetO|" + gson.toJson(configOptions);
                MainApplication.SendCommand(json);
            }
        });

        npEast = (NumberPicker)rootView.findViewById(R.id.maxEast);
        npEast.setMinValue(70);
        npEast.setMaxValue(110);
        npEast.setWrapSelectorWheel(true);
        npEast.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npEast.setValue(90);
        configTransfer.e = 90;
        npEast.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                configTransfer.e = newVal;
            }
        });
        npWest = (NumberPicker)rootView.findViewById(R.id.maxWest);
        npWest.setMinValue(240);
        npWest.setMaxValue(300);
        npWest.setWrapSelectorWheel(true);
        npWest.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npWest.setValue(270);
        configTransfer.w = 270;
        npWest.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                configTransfer.w = newVal;
            }
        });

        npMinElevation = (NumberPicker)rootView.findViewById(R.id.minElevation);
        npMinElevation.setMinValue(0);
        npMinElevation.setMaxValue(20);
        npMinElevation.setWrapSelectorWheel(true);
        npMinElevation.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npMinElevation.setValue(0);

        npMinElevation.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                configTransfer.n = newVal;
            }
        });
        npMaxElevation = (NumberPicker)rootView.findViewById(R.id.maxElevation);
        npMaxElevation.setMinValue(70);
        npMaxElevation.setMaxValue(110);
        npMaxElevation.setWrapSelectorWheel(true);
        npMaxElevation.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npMaxElevation.setValue(90);

        npMaxElevation.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                configTransfer.x = newVal;
            }
        });

        return rootView;
    }

    private BroadcastReceiver mConfigurationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra("json");
            Gson gson = new Gson();
            try {
                configTransfer.copy(gson.fromJson(json, ConfigTransfer.class));
                dualAxis.setChecked(configTransfer.d);
                lat.setText(String.format("%.6f", configTransfer.a));
                lon.setText(String.format("%.6f", configTransfer.o));
                npEast.setValue(configTransfer.e);
                npWest.setValue(configTransfer.w);
                npMinElevation.setValue(configTransfer.n);
                npMaxElevation.setValue(configTransfer.x);
                horizontalLength.setValue(configTransfer.lh);
                verticalLength.setValue(configTransfer.lv);
                horizontalSpeed.setValue(configTransfer.sh);
                verticalSpeed.setValue(configTransfer.sv);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

}