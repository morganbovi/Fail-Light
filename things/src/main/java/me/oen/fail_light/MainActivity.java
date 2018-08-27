package me.oen.fail_light;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import java.io.IOException;

import me.oen.fail_light.model.Failure;
import me.oen.fail_light.model.Lights;
import me.oen.fail_light.model.Success;
import me.oen.fail_light.model.Unstable;

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
    private DatabaseReference doSuccessRef;
    private DatabaseReference doUnstableRef;
    private DatabaseReference lightsRef;
    private Handler handler = new Handler();

    private boolean failIsRunning;
    private boolean successIsRunning;
    private boolean unstableIsRunning;

    private Lights mLights;

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
        doSuccessRef = database.getReference("success");
        doSuccessRef.setValue(new Success(false, DEAFULT_FAIL_FOR));
        doUnstableRef = database.getReference("unstable");
        doUnstableRef.setValue(new Unstable(false, DEAFULT_FAIL_FOR));
        lightsRef = database.getReference("lights");
    }

    public void initGPIO() {
        final PeripheralManager service = PeripheralManager.getInstance();

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

        doFailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Failure failure = dataSnapshot.getValue(Failure.class);
                try {
                    if (failure != null && !failIsRunning && !successIsRunning && !unstableIsRunning) {
                        if (failure.isDoFail()) {
                            failIsRunning = true;
                            gpio.setValue(false);

                            red.setValue(true);
                            green.setValue(false);
                            blue.setValue(false);

                            handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        failIsRunning = false;
                                        gpio.setValue(true);

                                        if (mLights != null) {
                                            if (mLights.getWhite()) {
                                                red.setValue(true);
                                                green.setValue(true);
                                                blue.setValue(true);
                                            } else {
                                                red.setValue(mLights.getRed());
                                                green.setValue(mLights.getGreen());
                                                blue.setValue(mLights.getBlue());
                                            }
                                        } else {
                                            red.setValue(false);
                                            green.setValue(false);
                                            blue.setValue(true);
                                        }

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

        doSuccessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Success success = dataSnapshot.getValue(Success.class);
                try {
                    if (success != null && !successIsRunning && !failIsRunning && !unstableIsRunning) {
                        if (success.getDoSuccess()) {
                            successIsRunning = true;

                            red.setValue(false);
                            green.setValue(true);
                            blue.setValue(false);

                            handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        successIsRunning = false;

                                        if (mLights != null) {
                                            if (mLights.getWhite()) {
                                                red.setValue(true);
                                                green.setValue(true);
                                                blue.setValue(true);
                                            } else {
                                                red.setValue(mLights.getRed());
                                                green.setValue(mLights.getGreen());
                                                blue.setValue(mLights.getBlue());
                                            }
                                        } else {
                                            red.setValue(false);
                                            green.setValue(false);
                                            blue.setValue(true);
                                        }

                                        doSuccessRef.setValue(new Success(false, DEAFULT_FAIL_FOR));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, success.getSucceedFor() * 1000);
                        } else {
                            handler.removeCallbacksAndMessages(null);
                        }
                    } else {
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

        doUnstableRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Unstable unstable = dataSnapshot.getValue(Unstable.class);
                try {
                    if (unstable != null && !successIsRunning && !failIsRunning && !unstableIsRunning) {
                        if (unstable.getDoUnstable()) {
                            unstableIsRunning = true;

                            red.setValue(true);
                            green.setValue(true);
                            blue.setValue(false);

                            handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        unstableIsRunning = false;

                                        if (mLights != null) {
                                            if (mLights.getWhite()) {
                                                red.setValue(true);
                                                green.setValue(true);
                                                blue.setValue(true);
                                            } else {
                                                red.setValue(mLights.getRed());
                                                green.setValue(mLights.getGreen());
                                                blue.setValue(mLights.getBlue());
                                            }
                                        } else {
                                            red.setValue(false);
                                            green.setValue(false);
                                            blue.setValue(true);
                                        }

                                        doUnstableRef.setValue(new Success(false, DEAFULT_FAIL_FOR));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, unstable.getUnstableFor() * 1000);
                        } else {
                            handler.removeCallbacksAndMessages(null);
                        }
                    } else {
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

        lightsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLights = dataSnapshot.getValue(Lights.class);
                try {
                    if (failIsRunning) {
                        return;
                    }

                    if (mLights.getWhite()) {
                        blue.setValue(true);
                        green.setValue(true);
                        red.setValue(true);
                        return;
                    } else {
                        blue.setValue(false);
                        green.setValue(false);
                        red.setValue(false);
                    }

                    red.setValue(mLights.getRed());
                    blue.setValue(mLights.getBlue());
                    green.setValue(mLights.getGreen());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            gpio.close();
            red.close();
            green.close();
            blue.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
