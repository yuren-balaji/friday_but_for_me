package com.example.first_app_0_0_1.search

import android.content.Context
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import kotlin.math.sqrt

class SemanticSearch(context: Context) {

    private val classifier: NLClassifier

    init {
        // TODO: Replace with your downloaded model from assets
        // Download a model like "universal-sentence-encoder-qa" from TensorFlow Hub
        val modelFile = "universal_sentence_encoder.tflite"
        classifier = NLClassifier.createFromFile(context, modelFile)
    }

    fun getVector(text: String): FloatArray {
        // This is a simplified example. The actual vector extraction might be different
        // depending on the model. For some models, you might need to use the C++ API
        // and JNI to get the sentence embeddings.
        // The NLClassifier is more for classification, but we use it here as a placeholder
        // to demonstrate the concept. A proper sentence encoder would be better.
        return classifier.classify(text).map { it.score }.toFloatArray()
    }

    fun findSimilar(query: String, topK: Int = 5): List<Pair<String, Float>> {
        val queryVector = getVector(query)
        val allVectors = VectorDatabase.getallVectors()

        return allVectors.map { (id, vector) ->
            id to cosineSimilarity(queryVector, vector)
        }.sortedByDescending { it.second }.take(topK)
    }

    private fun cosineSimilarity(vector1: FloatArray, vector2: FloatArray): Float {
        var dotProduct = 0.0f
        var norm1 = 0.0f
        var norm2 = 0.0f
        for (i in vector1.indices) {
            dotProduct += vector1[i] * vector2[i]
            norm1 += vector1[i] * vector1[i]
            norm2 += vector2[i] * vector2[i]
        }
        return dotProduct / (sqrt(norm1.toDouble()) * sqrt(norm2.toDouble())).toFloat()
    }
}
