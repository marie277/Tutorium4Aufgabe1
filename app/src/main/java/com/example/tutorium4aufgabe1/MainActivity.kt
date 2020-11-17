package com.example.tutorium4aufgabe1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager : SensorManager
    private lateinit var sensorAccelerometer : Sensor
    private var accelerometerData : SensorData? = null
    private val jsonArray = JSONArray()
    private lateinit var jsonArrayAsString:String
    private lateinit var jsonObjectAsString:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!=null) {
            sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        }
        val buttonStart: Button = findViewById(R.id.buttonStart)
        val buttonStop : Button = findViewById(R.id.buttonStop)
        //Start des Positionstrackings
        buttonStart.setOnClickListener(){
            registerListener()
        }
        //Stoppen des Positionstrackings
        buttonStop.setOnClickListener(){
            unregisterListener()
            sendJSON(jsonArrayAsString)
        }
    }
    private fun registerListener(){
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null){
            sensorManager.registerListener(this,sensorAccelerometer,SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    private fun unregisterListener(){
        sensorManager.unregisterListener(this,sensorAccelerometer)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
    override fun onSensorChanged(e: SensorEvent?) {
        val jsonObject = JSONObject()
        val tvTest: TextView = findViewById(R.id.tvTest)
        if(e?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            if (accelerometerData == null) {
                accelerometerData = SensorData(e.values.get(0), e.values.get(1), e.values.get(2), e.timestamp)
            } else {
                accelerometerData!!.x1 = e.values.get(0)
                accelerometerData!!.x2 = e.values.get(1)
                accelerometerData!!.x3 = e.values.get(2)
                accelerometerData!!.timestamp = e.timestamp
            }
            jsonObject.put("X","%.2f".format(e.values?.get(0)))
            jsonObject.put("Y","%.2f".format(e.values?.get(1)))
            jsonObject.put("Z","%.2f".format(e.values?.get(2)))
            jsonObjectAsString = jsonObject.toString()
            Log.d("JSON Object", jsonObjectAsString)
            jsonArray.put(jsonObject)
            jsonArrayAsString = jsonArray.toString()
            tvTest.text = jsonArrayAsString
            Log.d("JSON Array", jsonArrayAsString)
        }
    }
    private fun sendJSON(s: String){
        val client = OkHttpClient()
        val postBody = s
        val request = Request.Builder()
            .url("https://hsbo1.free.beeceptor.com")
            .post(postBody.toRequestBody())
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) throw IOException("Fehler: $response")
                Log.e("Res", response.body!!.string())
// Implementierung, was geschehen soll, wenn POST-Anfrage erfolgreich war

            }
        })
    }
}