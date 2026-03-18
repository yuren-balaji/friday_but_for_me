package com.example.first_app_0_0_1.data

import io.objectbox.annotation.Entity as ObjectBoxEntity
import io.objectbox.annotation.Id
import io.objectbox.annotation.HnswIndex

@ObjectBoxEntity
data class NoteVector(
    @Id var id: Long = 0,
    val noteId: String,
    @HnswIndex(dimensions = 384)
    var embedding: FloatArray? = null,
    val contentSnippet: String? = null
)
