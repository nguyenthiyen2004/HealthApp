package com.example.healthapp.fragments

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.healthapp.R
import com.google.firebase.database.FirebaseDatabase

class StepsFragment : Fragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private var steps: Int = 0
    private lateinit var tvSteps: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_steps, container, false)
        tvSteps = view.findViewById(R.id.tvSteps)

        // Khởi tạo SensorManager
        sensorManager = requireActivity().getSystemService(SensorManager::class.java)
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        return view
    }

    override fun onResume() {
        super.onResume()
        stepCounter?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            steps = event.values[0].toInt()
            tvSteps.text = "Steps: $steps"

            // Đồng bộ dữ liệu bước chân với Firebase
            val database = FirebaseDatabase.getInstance()
            val stepsRef = database.getReference("steps")
            stepsRef.setValue(steps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}