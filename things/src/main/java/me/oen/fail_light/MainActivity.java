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

import me.oen.fail_light.model.Failure;

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

    private static final int DEAFULT_FAIL_FOR = 5;

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
        doFailRef = database.getReference("failure");
        doFailRef.setValue(new Failure(false, DEAFULT_FAIL_FOR));
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
                Failure failure = dataSnapshot.getValue(Failure.class);
                try {
                    if (failure != null) {
                        if (failure.isDoFail()) {
                            gpio.setValue(false);
                            handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        gpio.setValue(true);
                                        doFailRef.setValue(new Failure(false, DEAFULT_FAIL_FOR));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, failure.getFailFor() * 1000);
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
    }
}
