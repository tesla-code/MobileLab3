package com.example.throwingball;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 *  User can set the threshold for detection.
 */
public class Settings extends AppCompatActivity {

    private SeekBar seekBar;
    private TextView value;
    private Button sub_button;
    private int threshold = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sub_button = findViewById(R.id.button_sub);
        sub_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent(); // getIntent();

                data.putExtra("threshold", threshold);
                setResult(RESULT_OK, data);

                Log.i("Settings", "Value of threshold at finish is: " + threshold);

                finish();
            }
        });

        // Create the seek bar
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMin(1);
        seekBar.setMax(100);
        value = findViewById(R.id.threshold_value);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value.setText("Threshold: " + String.valueOf(progress));
                threshold = Integer.parseInt(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
}
