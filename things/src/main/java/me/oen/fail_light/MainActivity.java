package me.oen.fail_light;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private Gpio gpio;
    private DatabaseReference doFailRef;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFirebase();
        initGPIO();
    }

    public void initFirebase() {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        doFailRef = database.getReference("doFail");
        doFailRef.setValue(false);
    }

    public void initGPIO() {
        PeripheralManagerService service = new PeripheralManagerService();

        try {
            gpio = service.openGpio("BCM14");
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            gpio.setActiveType(Gpio.ACTIVE_LOW);
            gpio.setValue(true);
        } catch (IOException e) {
            e.printStackTrace();
        }


        doFailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean lightOn = dataSnapshot.getValue(Boolean.class);
                try {
                    if (lightOn != null) {
                        if (lightOn) {
                            gpio.setValue(false);
                            handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        gpio.setValue(true);
                                        doFailRef.setValue(false);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 5000);
                        } else {
                            gpio.setValue(true);
                            handler.removeCallbacksAndMessages(null);
                        }
                    } else {
                        gpio.setValue(true);
                        handler.removeCallbacksAndMessages(null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                try {
                    gpio.setValue(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
//        for (int i = 0; i < 15; i++){
//            try {
//                gpio.setValue(!gpio.getValue());
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e)ture {
//                    e.printStackTrace();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

//        try {
//            Gpio gpio = service.openGpio("BCM14");
//            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
//            gpio.setActiveType(Gpio.ACTIVE_LOW);
//            gpio.setValue(true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
}
