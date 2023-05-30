package com.ks.dynamicrenderingmvvm.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_responses")
data class QuestionResponse( @PrimaryKey
                             val questionNumber: String,
                             val rating: Int,
                             val editableValue: String)
