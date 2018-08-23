package me.oen.fail_light;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import me.oen.fail_light.model.Failure;
import me.oen.fail_light.model.Lights;

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

    private Gpio green;
    private Gpio red;
    private Gpio blue;

    private DatabaseReference doFailRef;
    private DatabaseReference lights;
    private Handler handler = new Handler();

    private boolean isRunning;

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
        lights = database.getReference("lights");
        doFailRef.setValue(new Failure(false, DEAFULT_FAIL_FOR));
    }

    public void initGPIO() {
        PeripheralManager service = PeripheralManager.getInstance();

        try {
            gpio = service.openGpio("BCM14");
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            gpio.setActiveType(Gpio.ACTIVE_LOW);
            gpio.setValue(true);

            green = service.openGpio("BCM17");
            green.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            green.setActiveType(Gpio.ACTIVE_LOW);
            green.setValue(false);

            red = service.openGpio("BCM18");
            red.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            red.setActiveType(Gpio.ACTIVE_LOW);
            red.setValue(false);

            blue = service.openGpio("BCM27");
            blue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            blue.setActiveType(Gpio.ACTIVE_LOW);
            blue.setValue(false);
        } catch (IOException e) {
            e.printStackTrace();
        }


//        for (int i = 10; i > 0; i--) {
//            try {
//                green.setValue(true);
//                Thread.sleep(1000);
//                green.setValue(false);
//                Thread.sleep(1000);
//
//                red.setValue(true);
//                Thread.sleep(1000);
//                red.setValue(false);
//                Thread.sleep(1000);
//
//                blue.setValue(true);
//                Thread.sleep(1000);
//                blue.setValue(false);
//                Thread.sleep(1000);
//
//                blue.setValue(true);
//                green.setValue(true);
//                red.setValue(true);
//                Thread.sleep(5000);
//                blue.setValue(false);
//                green.setValue(false);
//                red.setValue(false);
//
//                Thread.sleep(3000);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }


        doFailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Failure failure = dataSnapshot.getValue(Failure.class);
                try {
                    if (failure != null && !isRunning) {
                        if (failure.isDoFail()) {
                            isRunning = true;
                            gpio.setValue(false);
                            handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        isRunning = false;
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

        lights.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Lights lights = dataSnapshot.getValue(Lights.class);
                try {
                    if (lights.getWhite()) {
                        blue.setValue(true);
                        green.setValue(true);
                        red.setValue(true);
                        return;
                    } else {
                        blue.setValue(false);
                        green.setValue(false);
                        red.setValue(false);
                    }

                    red.setValue(lights.getRed());
                    blue.setValue(lights.getBlue());
                    green.setValue(lights.getGreen());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            red.close();
            green.close();
            blue.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
