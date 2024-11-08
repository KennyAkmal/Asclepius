package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra("IMAGE_URI")?.let { Uri.parse(it) }
        val resultText = intent.getStringExtra("RESULT_TEXT")

        imageUri?.let { binding.resultImage.setImageURI(it) }
        binding.resultText.text = resultText?: "Hasil analisis tidak tersedia"
    }
}
