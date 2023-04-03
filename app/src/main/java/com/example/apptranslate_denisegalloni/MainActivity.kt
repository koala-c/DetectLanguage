package com.example.apptranslate_denisegalloni

import android.accounts.AuthenticatorDescription
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.apptranslate_denisegalloni.API.retrofitService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var btnDetectLanguage: Button
    private lateinit var etDescription: EditText
    private lateinit var progressBar: ProgressBar

    var allLanguages = emptyList<com.example.apptranslate_denisegalloni.Language>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initListener()
        getLanguages()
    }

    private fun initListener() {
        btnDetectLanguage.setOnClickListener {
            val text = etDescription.text.toString()
            if(text.isNotEmpty()) {
                showLoading()
                getTextLanguage(text)
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        runOnUiThread {
            progressBar.visibility = View.GONE
        }
    }

    private fun getTextLanguage(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = retrofitService.getTextLanguage(text)
            if(result.isSuccessful) {
                checkResult(result.body())
            } else {
                showError()
            }
            cleanText()
            hideLoading()
        }
    }

    private fun cleanText() {
        etDescription.setText("")
    }

    private fun checkResult(detectionResponse: DetectionResponse?) {
        if(detectionResponse != null && !detectionResponse.data.detections.isNullOrEmpty()) {
            val correctLanguages = detectionResponse.data.detections.filter { it.isReliable }
            if(correctLanguages.isNotEmpty()) {
                val languageName = allLanguages.find { it.code == correctLanguages.first().language }
                if(languageName != null) {
                    // Pero obtenir el context
                    runOnUiThread {
                        Toast.makeText(this, "El idioma es ${languageName.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getLanguages() {
        CoroutineScope(Dispatchers.IO).launch {
            val languages = retrofitService.getLanguages()
            if(languages.isSuccessful) {
                // Afegim el llistat de llenguatges, si no es una llista buida
                allLanguages = languages.body() ?: emptyList()
                showSuccess()
            } else {
                showError()
            }
        }
    }

    private fun showSuccess() {
        // En el fil principal
        runOnUiThread {
            Toast.makeText(this, "Petición correcta", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError() {
        // En el fil principal
        runOnUiThread {
            Toast.makeText(this, "Error al hacer la llamada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView() {
        btnDetectLanguage = findViewById(R.id.btnDetectaLanguage)
        etDescription = findViewById(R.id.etDescription)
        progressBar = findViewById(R.id.progressBar)
    }
}