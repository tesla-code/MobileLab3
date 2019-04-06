package com.example.throwingball;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * To add
 *      MIN_ACC is the acceleration Threadsheld variable
 *
 *      Extras.
 *         - High Score
 *
 *  To fix:
 *      - multiple throw events
 *      - Current height of ball not updating (i.e the text view wont update)
 *      - only registers higher throws
 *
 *      Line 120 ish     public void throwEvent(SensorEvent event) method.
 *      Throw event started at acceleration: 5.265211899759938
 *      Start of throw is done, Max acceleration was: 5.265211899759938
 */

/**
 * MainActivity Class
 *
 * @author Victor
 * @version 1
 * @since 25/03/2019
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final int REQUEST_CODE_TRANSFER = 101;

    // used to store all of the score results, this data will be sorted
    // inn the HighScore class
    static ArrayList<Double> highScoreList = new ArrayList<>();

    // Used to access the acceloromter
    private SensorManager sensorManager;
    private Sensor sensor;

    // Used to play sound
    MediaPlayer mediaPlayer = null; //  MediaPlayer.create(this, R.raw.pop);

    private TextView textView_x;
    private TextView textView_y;
    private TextView textView_z;
    private TextView textView_acc;
    private TextView textView_height_at_t;
    private TextView textView_score;

    private double maxAcceleration = 0;
    private double accelerationThreadsheld = 10.0;
    private double acceleration = 0;
    private double displacement = 0;
    private boolean inThrowEvent = false;

    private Button prefButton;
    private Button highScoreButton;

    // Acceleration due to gravity
    private double g = SensorManager.GRAVITY_EARTH; // g = 9.81 ish
    private double u;   // initial velocity, for us this will be the maxAcceleration

    // score of the last throw registerd.
    private double score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Assign preference button
        prefButton = (Button) findViewById(R.id.button_pref);
        prefButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivityForResult(intent, REQUEST_CODE_TRANSFER);
            }
        });

        // Assign the highscore button
        highScoreButton = (Button)  findViewById(R.id.button_highscore);
        highScoreButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                double[] highscoredata = new double[highScoreList.size()];

                for(int i = 0; i < highScoreList.size(); i++)
                {
                    highscoredata[i] = highScoreList.get(i);
                }

                Bundle b = new Bundle();
                b.putDoubleArray("highscore", highscoredata);
                Intent intent = new Intent(MainActivity.this, HighScore.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        // Assign values to text View
        textView_x = (TextView)findViewById(R.id.textView_x);
        textView_y = (TextView)findViewById(R.id.textView_y);
        textView_z = (TextView)findViewById(R.id.textView_z);

        textView_acc = (TextView)findViewById(R.id.textView_acc);
        textView_height_at_t = (TextView)findViewById(R.id.height_at_t);
        textView_score = (TextView)findViewById(R.id.textView_score);

        textView_height_at_t.setText("Current Height of ball is: " + displacement);
        setUpSoundPlayer();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        textView_x.setText("X: " + event.values[0]);
        textView_y.setText("Y: " + event.values[1]);
        textView_z.setText("Z: " + event.values[2]);

        // Calculate Acceleration using formula provided from the lab
        acceleration = Math.sqrt((event.values[0] * event.values[0])
                + (event.values[1] * event.values[1]) + (event.values[2] * event.values[2]) )
                - SensorManager.GRAVITY_EARTH;

        textView_acc.setText("Acceleration: " + acceleration);

        // If the acceleration is greater than the threshold the code starts an "throwEvent"
        if(accelerationThreadsheld <= acceleration && (inThrowEvent == false))
        {
            Log.i("ThrowEventStart" , "Event started at acceleration: " + acceleration);
            Log.i("ThrowEventStart", "bool is: " + inThrowEvent);
            inThrowEvent = true;
            throwEvent(event);
        }
    }

    /**
     * ThrowEvent method, the first 500ms is thought of as being in a throw,
     * and we find the highest acceleration between that point in time and store it.
     */
    public void throwEvent(SensorEvent event)
    {
        Log.i("ThrowEvent", "Throw event started at acceleration: " + acceleration);

        double start = System.currentTimeMillis();
        double delta = start;

        // The idea of this whole while loop is to start an
        // 500 ms throw, where the highest acceleration registered will
        // be the one that is used for the actual throw, i.e calculate ball height
        // score etc.
        while(start > (delta - 500))
        {
            delta = System.currentTimeMillis();

            // Calculate Acceleration
            acceleration = Math.sqrt((event.values[0] * event.values[0])
                    + (event.values[1] * event.values[1]) + (event.values[2] * event.values[2]) )
                    - SensorManager.GRAVITY_EARTH;

            if(maxAcceleration < acceleration)
            {
                maxAcceleration = acceleration;
                Log.i("ThrowEventInThrow", "New max acceleration is " + maxAcceleration);
            }
        }

        Log.i("ThrowEventInThrow", "Start of throw is done, Max acceleration is: " + maxAcceleration);

        // Calculates the time it takes the ball to reach the top of the throw
        // then it plays a sound
        double timeToTop = (maxAcceleration / SensorManager.GRAVITY_EARTH);
        timeToTop *= 1000;
        Log.i("ThrowEvent", "timeToTop is: " + timeToTop);
        Log.i("ThrowEvent", "Waiting " + timeToTop + "ms before playing sound");
        ballAscend(timeToTop);
        Log.i("ThrowEvent", "Plays sound after "+  timeToTop +
                " ms seconds to indicate height point of the ball");
        playSound();
        //Log.i("ThrowEvent", "Waiting " + timeToTop + "ms for the ball to get back down");
        waitMSDown(timeToTop);

        // calculate the score of the throw
        score =  Math.pow(maxAcceleration, 2) / (2 * SensorManager.GRAVITY_EARTH);

        highScoreList.add(score);

        // Display score
        textView_score.setText("Score: " + score);

        Log.i("ThorwEvent", "Score is: " + score);

        // ready to start new throw event
        inThrowEvent = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     *  Wait x amount of milliseconds before continuing, also calculates
     *  and displays current heigt of ball
     */
    public void ballAscend(double nrOfMSToWait) {
        double start = System.currentTimeMillis();
        double delta = System.currentTimeMillis();
        double time = delta - start;
        double counter = 0;

        /*
            This while loop
            - calculates displacement at time t
            - tries to display the current height in the text view
         */
        while(start > (delta - nrOfMSToWait))
        {
            // Calculate the displacement of the ball at time t.
            delta = System.currentTimeMillis();
            time = delta - start;
            counter = 0;

            Log.i("ThrowEvent", "time is " + time + " before loop");
            while (time >= 1000)
            {
                time -= 1000;
                counter++;
            }
            Log.i("ThrowEvent", "time is " + time + " after loop");

            // counter now contains the amount of whole seconds, should most likely be between
            // 0 and 3
            Log.i("ThrowEvent", "counter contains the value of " + counter);

            // we know that time is less than 1000, and we want the decimal value for time.
            // so to get the decimal part of the number we divide time by 1000.
            time /= 1000;
            time += counter;    // since counter contains the amount of whole seconds that have passed
            // the program simply adds them to the time value.

            Log.i("ThrowEvent", "time is (should contain decimals now) " + time);

            // calculate displacement of ball at a given time
            displacement = maxAcceleration / 2 * time;

            textView_height_at_t.setText("Current Height of ball is: " + displacement);
            Log.i("ThrowEvent", "Current Height of ball is: " + displacement);
        }
    }

    /**
     *  This method only waits for the given amount of milliseconds.
     * @param nrOfMSToWait
     */
    public void waitMSDown(double nrOfMSToWait) {
        double start = System.currentTimeMillis();
        double delta = System.currentTimeMillis();
        while(start > (delta - nrOfMSToWait)) {
            delta = System.currentTimeMillis();
        }
    }

    /**
     *  When the settings activity is done this method stores the entered value
     *  inn the variable.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TRANSFER) {
            if (resultCode == RESULT_OK) {

                // Get the new threshold value
                int threshold = data.getIntExtra("threshold", 0);
                Log.i("Settings", "Threshold is: " + threshold);
                accelerationThreadsheld = threshold;
            }
        }
    }

    /**
     *  Sets up the MediaPlayer, might change this method to a bool
     */
    public void  setUpSoundPlayer()
    {
        mediaPlayer = MediaPlayer.create(this, R.raw.pop);
    }

    /**
     *  Plays a sound, in this case a popping sound.
     */
    public void playSound()
    {
        mediaPlayer.start();
    }
}