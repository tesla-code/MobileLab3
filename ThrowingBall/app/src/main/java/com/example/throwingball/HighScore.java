package com.example.throwingball;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class HighScore extends AppCompatActivity {
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        Bundle b = this.getIntent().getExtras();
        double[] array=b.getDoubleArray("highscore");
        Double[] double_numbers= new Double[array.length];

        for(int i = 0; i < array.length; i++)
        {
            double_numbers[i] = array[i];
        }

        Arrays.sort(double_numbers, Collections.<Double>reverseOrder());

        String[] display = new String[double_numbers.length];

        for(int i = 0; i < double_numbers.length; i++)
        {
            display[i] = "Score: " + double_numbers[i];
        }

        // ArrayAdapter <Double> dataAdapter =
        //        new ArrayAdapter<Double>( this,android.R.layout.simple_spinner_item,double_numbers);

        ArrayAdapter <String> dataAdapter2 =
                new ArrayAdapter<String>( this,android.R.layout.simple_spinner_item, display);

        listView = findViewById(R.id.high_score_list);
        listView.setAdapter(dataAdapter2);

    }
}
