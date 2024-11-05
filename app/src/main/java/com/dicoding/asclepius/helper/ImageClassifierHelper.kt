package com.dicoding.asclepius.helper

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.dicoding.asclepius.R
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions
import java.io.IOException

class ImageClassifierHelper(
    private val threshold: Float = 0.1f,
    private val maxResult: Int = 3,
    private val modelName: String = "cancer_classification.tflite",
    private val context: Context,
    private val classifierListener: ClassifierListener?
) {
    private var imageClassifier: ImageClassifier? = null

    interface ClassifierListener {
        fun onError(error: String)
        fun onResult(result: List<Classifications>?, inferenceTime: Long)
    }

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val options = ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResult)
            .setBaseOptions(BaseOptions.builder().setNumThreads(2).build())
            .build()

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(context, modelName, options)
        } catch (e: IOException) {
            classifierListener?.onError("Error initializing classifier: ${e.localizedMessage}")
            Log.e(TAG, "Error initializing classifier: ${e.message}")
        } catch (e: IllegalStateException) {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
            Log.e(TAG, "Error initializing classifier: ${e.message}")
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        try {
            val bitmap = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }

            if (bitmap != null) {
                val tensorImage = TensorImage.fromBitmap(bitmap)
                val startTime = System.currentTimeMillis()
                val results = imageClassifier?.classify(tensorImage)
                val inferenceTime = System.currentTimeMillis() - startTime

                if (results.isNullOrEmpty()) {
                    classifierListener?.onError("No classifications detected.")
                } else {
                    classifierListener?.onResult(results, inferenceTime)
                }
            } else {
                classifierListener?.onError("Failed to load image.")
            }
        } catch (e: IOException) {
            classifierListener?.onError("Error reading image: ${e.localizedMessage}")
            Log.e(TAG, "Error reading image: ${e.message}")
        }
    }
}
