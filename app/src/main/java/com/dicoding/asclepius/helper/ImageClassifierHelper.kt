package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.IOException

class ImageClassifierHelper(
    private val context: Context,
    private val classifierListener: ClassifierListener?,
    private val modelName: String = "cancer_classification.tflite",
    private val threshold: Float = 0.1f,
    private val maxResult: Int = 3
) {
    private var imageClassifier: ImageClassifier? = null

    interface ClassifierListener {
        fun onError(error: String)
        fun onResult(resultText: String)
    }

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResult)
            .setBaseOptions(BaseOptions.builder().setNumThreads(2).build())
            .build()

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(context, modelName, options)
        } catch (e: IOException) {
            classifierListener?.onError("Error :${e.localizedMessage}")
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        try {
            val bitmap = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
            if (bitmap != null) {
                val tensorImage = TensorImage.fromBitmap(bitmap)
                val classifications = imageClassifier?.classify(tensorImage)

                if (classifications.isNullOrEmpty()) {
                    classifierListener?.onError("Tidak dapat melakukan klasifikasi")
                } else {
                    val highestConfidenceScore = classifications[0].categories.maxByOrNull { it.score }
                    val resultText = highestConfidenceScore?.let { category ->
                        "${category.label}: ${(category.score * 100).toInt()}%"
                    } ?: "Hasil analisis tidak tersedia"

                    classifierListener?.onResult(resultText)
                }
            } else {
                classifierListener?.onError("Gagal memuat gambar")
            }
        } catch (e: IOException) {
            classifierListener?.onError("Error: ${e.localizedMessage}")
        }
    }
}
