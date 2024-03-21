package com.example.lilinyxzhao_horchatas_springbreakchooserapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lilinyxzhao_horchatas_springbreakchooserapplication.ui.theme.LiliNyxZhaoHorchatasSpringBreakChooserApplicationTheme
import java.util.Locale
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private val threshold = 10
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent
    private lateinit var editTextPhrase: EditText
    private lateinit var selectedLanguage: String
//    private var longitude: Double = 0.0
//    private var latitude: Double = 0.0
    private lateinit var vacationSpotDetails: VacationSpot
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
        editTextPhrase = findViewById(R.id.editTextPhrase);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }
        val languageSelector = findViewById<ComposeView>(R.id.languageSelector)
        languageSelector.setContent {
            LiliNyxZhaoHorchatasSpringBreakChooserApplicationTheme {
                LanguageSelector (
                    onLanguageSelected = { selectedLanguage, randomSpot->
                    startListening()
//                    this@MainActivity.selectedLanguage = selectedLanguage
//                    this@MainActivity.longitude = longitude
//                    this@MainActivity.latitude = latitude
                    launchGoogleMapsForVacationSpot(selectedLanguage, randomSpot)
                },
                    getVacationSpotForLanguage = {language -> getVacationSpotForLanguage(language)}
                )
            }
        }
//        languageSelector.setOnClickListener {
//            speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//            }
//        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        sensorManager?.registerListener(sensorListener,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL)
    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening()
        }
    }
    private fun startListening() {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {
                runOnUiThread {
                    editTextPhrase.setText("")
                    editTextPhrase.hint = "Listening..."
                }
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                    if (it.isNotEmpty()) {
                        val spokenText = it[0]
                        Log.d("SpeechRecognition", "Recognized Text: $spokenText")
                        runOnUiThread {
                            editTextPhrase.setText(spokenText)
                        }
                    }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        speechRecognizer.setRecognitionListener(listener)
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > threshold) {
                launchGoogleMapsForVacationSpot(selectedLanguage, vacationSpotDetails) // Launch Google Maps
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

//    private fun launchGoogleMapsForVacationSpot(selectedLanguage: String, longitude: Double, latitude: Double) {
//        // Launch Google Maps with geo URI for the determined vacation spot
//        val gmmIntentUri = "geo:$latitude,$longitude?q=${getVacationSpotName(selectedLanguage, longitude, latitude)}"
//        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(gmmIntentUri))
//        mapIntent.setPackage("com.google.android.apps.maps")
//        startActivity(mapIntent)
//    }
//
//    private fun getVacationSpotName(language: String, longitude: Double, latitude: Double): String {
//        // Determine the name of the vacation spot based on the selected language and coordinates
//        val vacationSpots = getVacationSpotForLanguage(language)
//        return vacationSpots.find { it.longitude == longitude && it.latitude == latitude }?.name ?: "Unknown"
//    }

    private fun launchGoogleMapsForVacationSpot(selectedLanguage: String, vacationSpotDetails: VacationSpot) {
        // Determine vacation spot based on the selected language (e.g., Spanish)
//        val vacationSpot = getVacationSpotForLanguage(selectedLanguage)

        // Launch Google Maps with geo URI for the determined vacation spot
        val gmmIntentUri = "geo:${vacationSpotDetails.latitude},${vacationSpotDetails.longitude}?q=${vacationSpotDetails.name}"
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(gmmIntentUri))
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun getVacationSpotForLanguage(language: String): List<VacationSpot> {
        return when (language) {

            "English" -> listOf(
                VacationSpot("London", 51.5074, -0.1278),
                VacationSpot("New York City", 40.7128, -74.0060),
                VacationSpot("Sydney", -33.8688, 151.2093)
            )
            "Arabic" -> listOf(
                VacationSpot("Cairo", 30.0330, 31.2336),
                VacationSpot("Dubai", 25.276987, 55.296249),
                VacationSpot("Istanbul", 41.0082, 28.9784)
            )
            "Spanish" -> listOf(
                VacationSpot("Mexico City", 19.4326, -99.1332),
                VacationSpot("Barcelona", 41.3851, 2.1734),
                VacationSpot("Buenos Aires", -34.6037, -58.3816)
            )
            "French" -> listOf(
                VacationSpot("Paris", 48.8566, 2.3522),
                VacationSpot("Montreal", 45.5017, -73.5673),
                VacationSpot("Nice", 43.7102, 7.2620)
            )
            "Mandarin" -> listOf(
                VacationSpot("Beijing", 39.9042, 116.4074),
                VacationSpot("Shanghai", 31.2304, 121.4737),
                VacationSpot("Taipei", 25.0320, 121.5654)
            )

            else -> listOf(VacationSpot("Default Location", 0.0, 0.0))
        }
    }
}

@Composable
fun LanguageSelector(onLanguageSelected: (String, VacationSpot) -> Unit, getVacationSpotForLanguage: (String)->List<VacationSpot>) {
    val languages = listOf("English", "Arabic", "Spanish", "French", "Mandarin")
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(languages[0]) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentSize(Alignment.BottomEnd)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Choose Language: $selectedLanguage", modifier = Modifier.padding(end = 8.dp))
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Language Options")
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            selectedLanguage = language
                            expanded = false
                            val spots = getVacationSpotForLanguage(language)
                            val randomSpot = spots.random()
                            onLanguageSelected(language, randomSpot)
                        }
                    )
                }
            }
        }
    }
}


data class VacationSpot(val name: String, val latitude: Double, val longitude: Double)

