package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var classifierHelper: ImageClassifierHelper
    private var currentImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? -> uri?.let {
            currentImageUri = it
            binding.previewImageView.setImageURI(uri)
            binding.analyzeButton.isEnabled = true
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        classifierHelper = ImageClassifierHelper(context = this, classifierListener = this)
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener { analyzeImage() }
    }
    private fun startGallery() {
        galleryLauncher.launch("image/*")
    }
    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            classifierHelper.classifyStaticImage(uri)
        } ?: run {
            Toast.makeText(this,"Pilih file image terlebih dahulu",
                Toast.LENGTH_SHORT).show()
        }
    }
    override fun onResult(resultText: String) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("IMAGE_URI", currentImageUri.toString())
            putExtra("RESULT_TEXT", resultText)
        }
        startActivity(intent)
    }
    override fun onError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }
}
