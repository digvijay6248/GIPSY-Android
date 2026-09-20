package com.gipsy.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var chat: TextView
    private lateinit var input: EditText
    private lateinit var tts: TextToSpeech
    private var recognizer: SpeechRecognizer? = null
    private val micCode = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        buildUi()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), micCode)
        }
        addGipsy("GIPSY online. Tap 🎙 to speak or type a command.")
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 20)
            setBackgroundResource(com.gipsy.assistant.R.drawable.gipsy_bg)
        }

        val title = TextView(this).apply {
            text = "GIPSY"
            textSize = 30f
            setTextColor(0xFFF2F5FA.toInt())
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val subtitle = TextView(this).apply {
            text = "General Intelligent Personal System for You"
            textSize = 13f
            setTextColor(0xFF8D99AA.toInt())
            gravity = Gravity.CENTER_HORIZONTAL
        }

        chat = TextView(this).apply {
            textSize = 16f
            setTextColor(0xFFF2F5FA.toInt())
            setPadding(16, 20, 16, 20)
        }
        val scroll = ScrollView(this).apply { addView(chat) }

        input = EditText(this).apply {
            hint = "Ask GIPSY..."
            setSingleLine(true)
            setTextColor(0xFFF2F5FA.toInt())
            setHintTextColor(0xFF8D99AA.toInt())
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val mic = Button(this).apply {
            text = "🎙"
            setOnClickListener { listen() }
        }
        val send = Button(this).apply {
            text = "Send"
            setOnClickListener { process(input.text.toString()) }
        }
        row.addView(input, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(mic)
        row.addView(send)

        root.addView(title)
        root.addView(subtitle)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(row)
        setContentView(root)
    }

    private fun addUser(text: String) {
        chat.append("\nYou: $text\n")
    }

    private fun addGipsy(text: String) {
        chat.append("\nGIPSY: $text\n")
        speak(text)
    }

    private fun process(raw: String) {
        val text = raw.trim()
        if (text.isEmpty()) return
        input.setText("")
        addUser(text)
        val lower = text.lowercase(Locale.getDefault())

        when {
            lower in listOf("hello", "hi", "hey") ->
                addGipsy("Hello, Chief. I'm ready.")
            lower.contains("time") ->
                addGipsy(java.text.SimpleDateFormat("h:mm a", Locale.getDefault()).format(java.util.Date()))
            lower.startsWith("open ") ->
                openAppOrWeb(text.substringAfter("open ").trim())
            lower.startsWith("search ") ->
                webSearch(text.substringAfter("search ").trim())
            else ->
                addGipsy("I understood your command, but that skill is not installed yet. The Android version is ready for more skills.")
        }
    }

    private fun openAppOrWeb(target: String) {
        val pm = packageManager
        val apps = pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
        )
        val match = apps.firstOrNull {
            it.loadLabel(pm).toString().equals(target, ignoreCase = true)
        }
        if (match != null) {
            val launch = pm.getLaunchIntentForPackage(match.activityInfo.packageName)
            if (launch != null) {
                startActivity(launch)
                addGipsy("Opening $target.")
                return
            }
        }
        webSearch(target)
    }

    private fun webSearch(query: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra("query", query)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW,
                android.net.Uri.parse("https://www.google.com/search?q=" +
                    android.net.Uri.encode(query))))
        }
        addGipsy("Searching for $query.")
    }

    private fun listen() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            addGipsy("Speech recognition is not available on this device.")
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    addGipsy("I couldn't hear that. Please try again.")
                }
                override fun onResults(results: Bundle?) {
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!text.isNullOrBlank()) process(text)
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            })
        }
    }

    private fun speak(text: String) {
        if (::tts.isInitialized) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "gipsy")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts.language = Locale("en", "IN")
    }

    override fun onDestroy() {
        recognizer?.destroy()
        tts.shutdown()
        super.onDestroy()
    }
}
