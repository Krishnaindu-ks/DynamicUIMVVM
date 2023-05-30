package com.ks.dynamicrenderingmvvm.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
@Dao
interface QuestionResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(questionResponse: QuestionResponse)

    @Query("SELECT * FROM question_responses")
    fun getAllQuestionResponses():List<QuestionResponse>
}