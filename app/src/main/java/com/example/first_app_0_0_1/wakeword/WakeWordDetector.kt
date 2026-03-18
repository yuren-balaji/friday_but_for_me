package com.example.first_app_0_0_1.wakeword

import android.content.Context
import android.media.AudioRecord
import android.util.Log
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class WakeWordDetector(
    private val context: Context,
    private val onWakeWordDetected: () -> Unit
) {
    private var classifier: AudioClassifier? = null
    private var audioRecord: AudioRecord? = null
    private var executor: ScheduledThreadPoolExecutor? = null

    init {
        try {
            // TODO: Replace with your custom wake word model
            val modelFile = "wake_word_model.tflite"
            classifier = AudioClassifier.createFromFile(context, modelFile)
        } catch (e: Exception) {
            Log.e("WakeWordDetector", "Error initializing AudioClassifier", e)
        }
    }

    fun start() {
        val currentClassifier = classifier
        if (currentClassifier == null) {
            Log.e("WakeWordDetector", "AudioClassifier not initialized.")
            return
        }

        val record = currentClassifier.createAudioRecord()
        audioRecord = record

        record.startRecording()

        executor = ScheduledThreadPoolExecutor(1)
        executor?.scheduleAtFixedRate({
            val tensor = currentClassifier.createInputTensorAudio()
            tensor.load(record)
            val results = currentClassifier.classify(tensor)

            results.forEach { result ->
                result.categories.forEach { category ->
                    if (category.label == "your_wake_word_label" && category.score > 0.8) {
                        onWakeWordDetected()
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS) // Adjust the period as needed
    }

    fun stop() {
        executor?.shutdownNow()
        audioRecord?.stop()
        audioRecord = null
        executor = null
    }
}
