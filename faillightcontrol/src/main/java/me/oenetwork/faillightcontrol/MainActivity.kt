package me.oenetwork.faillightcontrol

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import me.oenetwork.faillightcontrol.model.Failure
import me.oenetwork.faillightcontrol.model.Lights
import me.oenetwork.faillightcontrol.model.Success

class MainActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private var doFailRef = database.getReference("failure")
    private var doSuccessRef = database.getReference("success")
    private var lightsRef = database.getReference("lights")

    private var lights = Lights()

//    private var lightsRef

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lightsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val lights = p0.getValue(Lights::class.java)
                try {
                    lights?.run {
                        this@MainActivity.lights = lights
                        blue_button.text = if (lights.blue) "on" else "off"
                        red_button.text = if (lights.red) "on" else "off"
                        green_button.text = if (lights.green) "on" else "off"
                        white_button.text = if (lights.white) "on" else "off"
                    }
                } catch (e: Exception) {
                    Log.e(this::class.java.name, "Oh Boy", e)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e(this::class.java.name, "Oh Boy", p0.toException())
            }
        })

        blue_button.setOnClickListener {
            lightsRef.setValue(lights.copy(
                    blue = !lights.blue
            ))
        }

        red_button.setOnClickListener {
            lightsRef.setValue(lights.copy(
                    red = !lights.red
            ))
        }

        green_button.setOnClickListener {
            lightsRef.setValue(lights.copy(
                    green = !lights.green
            ))
        }

        white_button.setOnClickListener {
            lightsRef.setValue(lights.copy(
                    white = !lights.white
            ))
        }

        fail_button.setOnClickListener {
            doFailRef.setValue(Failure(true, 10))
        }

        succeed_button .setOnClickListener {
            doSuccessRef.setValue(Success(true, 10))
        }

    }

}
