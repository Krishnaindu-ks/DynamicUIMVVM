package com.ks.dynamicrenderingmvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ks.dynamicrenderingmvvm.model.QuestionRepository

class QuestionViewModelFactory(private val questionRepository: QuestionRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestionViewModel(questionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
