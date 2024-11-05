package com.dicoding.asclepius.helper

import org.tensorflow.lite.support.image.ImageProcessor
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.util.Log
import com.dicoding.asclepius.R
import org.tensorflow.lite.TensorFlowLite.init
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.lang.Error
import java.lang.IllegalStateException


class ImageClassifierHelper(
    var threshold: Float = 0.1f,
    var maxresult : Int = 3,
    val modelName:String = "cancer_classification",
    val context: Context,
    val classifierListener: ClassifierListener?
) {
    private var imageClassifier:ImageClassifier? = null
    interface ClassifierListener {
        fun onError(error:String)
        fun onResult(
            result: List<Classifications>?,
            inferenceTime: Long
        )
    }
    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val optionBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxresult)
        val baseOptionBuilder = BaseOptions.builder()
            .setNumThreads(0)
        optionBuilder.setBaseOptions(baseOptionBuilder.build())

        try{
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionBuilder.build()
            )
        } catch (e: IllegalStateException){
            classifierListener?.onError(context.getString(R.string.result))
            Log.e(TAG, e.message.toString())
        }
    }

    companion object {
        private const val TAG = = "ImageClassifierHelper"
    }


    fun classifyStaticImage(imageUri: Uri) {
        // TODO: mengklasifikasikan imageUri dari gambar statis.
    }

}