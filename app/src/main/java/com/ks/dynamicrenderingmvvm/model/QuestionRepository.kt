package com.ks.dynamicrenderingmvvm.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuestionRepository(private val questionResponseDao: QuestionResponseDao) {
   suspend fun storeQuestionResponse(questionResponse: QuestionResponse){
       withContext(Dispatchers.IO) {
           questionResponseDao.insert(questionResponse)
       }
    }

   suspend fun getAllQuestionResponses(): List<QuestionResponse> {
        return withContext(Dispatchers.IO) {
            questionResponseDao.getAllQuestionResponses()
        }
    }
}