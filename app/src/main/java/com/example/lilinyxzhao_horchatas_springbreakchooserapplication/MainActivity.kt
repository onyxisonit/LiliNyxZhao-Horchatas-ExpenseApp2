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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
                LanguageSelector { selectedLanguage ->
                    startListening()
                }
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
                launchGoogleMapsForVacationSpot() // Launch Google Maps
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun launchGoogleMapsForVacationSpot() {
        // Determine vacation spot based on the selected language (e.g., Spanish)
        val vacationSpot = getVacationSpotForLanguage("Spanish")

        // Launch Google Maps with geo URI for the determined vacation spot
        val gmmIntentUri = "geo:${vacationSpot.latitude},${vacationSpot.longitude}?q=${vacationSpot.name}"
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(gmmIntentUri))
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun getVacationSpotForLanguage(language: String): VacationSpot {
        // Determine the vacation spot based on the selected language
        // For simplicity, return a hardcoded location
        return when (language) {
            "Spanish" -> VacationSpot("Mexico City", 19.4326, -99.1332)
            // Add more cases for other languages
            else -> VacationSpot("Default Location", 0.0, 0.0) // Default location if language is not recognized
        }
    }
}

@Composable
fun LanguageSelector(onLanguageSelected: (String) -> Unit) {
    val languages = listOf("English", "Arabic", "Spanish", "French", "Chinese")
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(languages[0]) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentSize(Alignment.TopEnd)
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
                            onLanguageSelected(language)
                        }
                    )
                }
            }
        }
    }
}
//@Preview
//@Composable
//fun LanguageSelector() {
//    val languages = listOf("English", "Arabic", "Spanish", "French", "Chinese")
//    val context = LocalContext.current
//    var expanded by remember { mutableStateOf(false) }
//    var selectedLanguage by remember { mutableStateOf(languages[0]) }
//
////    Column {
////        Button(onClick = { expanded = true }) {
////            Text("Choose Language")
////        }
//    Box(
//        modifier = Modifier.fillMaxWidth()
//            .wrapContentSize(Alignment.TopEnd)
//    ) {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Text("Choose Language", modifier = Modifier.padding(end = 8.dp))
//            IconButton(onClick = { expanded = !expanded }) {
//                Icon(
//                    imageVector = Icons.Default.MoreVert,
//                    contentDescription = "Language Options"
//                )
//            }
//        }
//
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            //offset = DpOffset(x = 20.dp, y = 40.dp)
//        ) {
//            languages.forEach { language ->
//                DropdownMenuItem(
//                    text = { Text(language) },
//                    onClick = {
////                        selectedLanguage = language
//                        expanded = false
//                    }
//                )
//            }
//        }
////        Text("Selected language: $selectedLanguage")
//    }
//}

data class VacationSpot(val name: String, val latitude: Double, val longitude: Double)

