package com.ks.dynamicrenderingmvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ks.dynamicrenderingmvvm.model.QuestionRepository
import com.ks.dynamicrenderingmvvm.model.QuestionResponse


import kotlinx.coroutines.launch

class QuestionViewModel(private var questionRepository: QuestionRepository) : ViewModel() {


   fun storeQuestionResponse(questionNumber: String, rating: Int, editableValue: String) {
        val questionResponse = QuestionResponse(questionNumber, rating, editableValue)
        viewModelScope.launch {
            questionRepository.storeQuestionResponse(questionResponse)
        }
    }

   suspend fun getAllQuestionResponses(): List<QuestionResponse> {
        return questionRepository.getAllQuestionResponses()
    }

}