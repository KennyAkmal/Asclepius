package com.dicoding.asclepius.view

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.ml.CancerClassification
import org.tensorflow.lite.support.image.TensorImage

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null

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

        binding.progressIndicator.visibility = View.GONE
        binding.analyzeButton.isEnabled = false

        binding.galleryButton.setOnClickListener { openGallery() }
        binding.analyzeButton.setOnClickListener { analyzeImage() }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            binding.progressIndicator.visibility = View.VISIBLE
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            classifyImageWithCancerModel(bitmap)
        } ?: showToast("No image selected.")
    }

    private fun classifyImageWithCancerModel(bitmap: Bitmap) {
        val model = CancerClassification.newInstance(this)

        val tensorImage = TensorImage.fromBitmap(bitmap)
        val outputs = model.process(tensorImage)
        val probabilities = outputs.probabilityAsCategoryList

        model.close()

        binding.progressIndicator.visibility = View.GONE
        val resultText = probabilities.joinToString("\n") { category ->
            "Label: ${category.label}, Confidence: ${(category.score * 100).toInt()}%"
        }


        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("IMAGE_URI", currentImageUri.toString())
            putExtra("RESULT_TEXT", resultText)
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
