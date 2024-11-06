package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var classifierHelper: ImageClassifierHelper
    private var currentImageUri: Uri? = null

    // Launcher untuk memilih gambar dari galeri
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
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
        binding.galleryButton.setOnClickListener { openGallery() }
        binding.analyzeButton.setOnClickListener { analyzeImage() }
    }

    // Fungsi untuk membuka galeri
    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    // Fungsi untuk menganalisis gambar setelah dipilih
    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            classifierHelper.classifyStaticImage(uri)
        } ?: run {
            showToast("No image selected.")
        }
    }

    override fun onResult(resultText: String) {

        // Mengirimkan data ke ResultActivity
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("IMAGE_URI", currentImageUri.toString())  // Kirimkan URI gambar
            putExtra("RESULT_TEXT", resultText)  // Kirimkan hasil analisis
        }
        startActivity(intent)
    }

    override fun onError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
