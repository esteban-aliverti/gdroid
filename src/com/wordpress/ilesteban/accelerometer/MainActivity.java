package com.wordpress.ilesteban.accelerometer;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener, SensorEventListener, CoordinateDataSource, ExceptionListener {

    private SeekBar seekX;
    private SeekBar seekY;
    private SeekBar seekZ;
    private TextView txtX;
    private TextView txtY;
    private TextView txtZ;
    private DecimalFormat twoDForm = new DecimalFormat("#.#####");
    private ToggleButton btnUseSimulator;
    private EditText txtIp;
    private EditText txtPort;
    private ToggleButton btnStart;
    
    private DataSender dataSender;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.seekX = (SeekBar) findViewById(R.id.seekX);
        this.seekY = (SeekBar) findViewById(R.id.seekY);
        this.seekZ = (SeekBar) findViewById(R.id.seekZ);

        this.seekX.setOnSeekBarChangeListener(this);
        this.seekY.setOnSeekBarChangeListener(this);
        this.seekZ.setOnSeekBarChangeListener(this);

        this.txtX = (TextView) findViewById(R.id.txtX);
        this.txtY = (TextView) findViewById(R.id.txtY);
        this.txtZ = (TextView) findViewById(R.id.txtZ);
        
        this.txtIp = (EditText) findViewById(R.id.txtServerIp);
        this.txtPort = (EditText) findViewById(R.id.txtServerPort);

        this.btnUseSimulator = (ToggleButton) findViewById(R.id.btnUseSimulator);
        this.btnUseSimulator.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (btnUseSimulator.isChecked()) {
                    useSimulator();
                } else {
                    useAccelerometer();
                }
            }
        });

        this.btnUseSimulator.setChecked(true);

        this.btnStart = (ToggleButton) findViewById(R.id.btnStart);
        this.btnStart.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (btnStart.isChecked()) {
                    start();
                } else {
                    stop();
                }
            }
        });
        
        this.txtX.setText(seekValueToG(this.seekX.getProgress())+"");
        this.txtY.setText(seekValueToG(this.seekY.getProgress())+"");
        this.txtZ.setText(seekValueToG(this.seekZ.getProgress())+"");
    }

    public void onProgressChanged(SeekBar seekBar, int value, boolean arg2) {
        double transformedValue = this.seekValueToG(value);

        TextView txtView = null;
        switch (seekBar.getId()) {
            case R.id.seekX:
                txtView = this.txtX;
                break;
            case R.id.seekY:
                txtView = this.txtY;
                break;
            case R.id.seekZ:
                txtView = this.txtZ;
                break;
        }

        txtView.setText(twoDForm.format(transformedValue));

    }

    public void onSensorChanged(SensorEvent event) {
        if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {
            this.seekX.setProgress(gToSeekValue(event.values[0]));
            this.seekY.setProgress(gToSeekValue(event.values[1]));
            this.seekZ.setProgress(gToSeekValue(event.values[2]));
        }
    }

    public void onStartTrackingTouch(SeekBar arg0) {
    }

    public void onStopTrackingTouch(SeekBar arg0) {
    }

    private double seekValueToG(int seekValue) {
        return seekValue * 9.8 / 300.0;
    }

    private int gToSeekValue(float gValue) {
        return (int) (gValue * 300.0 / 9.8);
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    public float getX() {
        return Float.parseFloat(this.txtX.getText().toString());
    }

    public float getY() {
        return Float.parseFloat(this.txtY.getText().toString());
    }

    public float getZ() {
        return Float.parseFloat(this.txtZ.getText().toString());
    }
    
    private void useSimulator(){
        Toast.makeText(MainActivity.this, "Simulator is now active", Toast.LENGTH_SHORT).show();
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(MainActivity.this);
        
        seekX.setEnabled(true);
        seekY.setEnabled(true);
        seekZ.setEnabled(true);
        
    }
    
    private void useAccelerometer(){
        Toast.makeText(MainActivity.this, "Accelerometer is now active", Toast.LENGTH_SHORT).show();
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(MainActivity.this,
            sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL);
        
        seekX.setEnabled(false);
        seekY.setEnabled(false);
        seekZ.setEnabled(false);
        
    }
    
    private void start(){
        String ip = txtIp.getText().toString();
        if (ip.trim().equals("")){
            Toast.makeText(MainActivity.this, "Invalid Host", Toast.LENGTH_SHORT).show();
            btnStart.setChecked(false);
            return;
        }
        
        int port;
        try{
            port = Integer.parseInt(txtPort.getText().toString());
        }catch(Exception e){
            Toast.makeText(MainActivity.this, "Invalid Port", Toast.LENGTH_SHORT).show();
            btnStart.setChecked(false);
            return;
        }
        
        txtIp.setEnabled(false);
        txtPort.setEnabled(false);
        
        if (dataSender != null){
            dataSender.stop();
        }
        try {
            dataSender = new DataSender("Android", 100, ip, port, this);
            dataSender.setExceptionListener(this);
        } catch (UnknownHostException ex) {
            btnStart.setChecked(false);
            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        
        Thread t = new Thread(dataSender);
        t.start();
        
        Toast.makeText(MainActivity.this, "Server Running", Toast.LENGTH_SHORT).show();
        
    }
    
    private void stop(){
        
        if (dataSender != null){
            dataSender.stop();
        }
        
        Toast.makeText(MainActivity.this, "Server Stopped", Toast.LENGTH_SHORT).show();
        txtIp.setEnabled(true);
        txtPort.setEnabled(true);
    }

    public void onException(Exception ex) {
        Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
    }
}
