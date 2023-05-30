package com.ks.dynamicrenderingmvvm.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.google.gson.Gson
import com.ks.dynamicrenderingmvvm.model.AppDatabase
import com.ks.dynamicrenderingmvvm.model.QuestionRepository
import com.ks.dynamicrenderingmvvm.model.QuestionResponse
import com.ks.dynamicrenderingmvvm.ui.theme.DynamicRenderingMVVMTheme
import com.ks.dynamicrenderingmvvm.viewmodel.QuestionViewModel
import com.ks.dynamicrenderingmvvm.viewmodel.QuestionViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Question(
    val questionNumber:String,
    val questionTitle: String,
    val questionType: String,
    val questionOptional: Boolean
)


class MainActivity : ComponentActivity() {
    private val jsonString = """[
    {    "questionNumber": "1",
        "questionTitle": "Please provide a rating",
        "questionType": "RATING",
        "questionOptional": true
    },
    {    "questionNumber": "2",
        "questionTitle": "Describe your feedback",
        "questionType": "EDITABLE",
        "questionOptional": true
    }
     
]"""
    private lateinit var questionViewModel: QuestionViewModel
    private lateinit var questionRepository: QuestionRepository
    private val gson = Gson()
    private val questionList = gson.fromJson(jsonString, Array<Question>::class.java).toList()
    private lateinit var database: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
            .build()

        questionRepository = QuestionRepository(database.questionResponseDao())
        questionViewModel = ViewModelProvider(
            this,
            QuestionViewModelFactory(questionRepository)
        ).get(QuestionViewModel::class.java)

        setContent {

            DynamicRenderingMVVMTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "questionList") {
                    composable("questionList") {
                        QuestionList(
                            modifier = Modifier,
                            questionList = questionList,
                            onNextClicked = { navController.navigate("nextPage") },
                            onQuestionResponseSubmitted = { questionNumber, rating, editableValue ->
                                questionViewModel.storeQuestionResponse(questionNumber, rating, editableValue)
                            },

                        )

                    }
                    composable("nextPage") {

                        NextPage( questionList= questionList)
                    }

                }

            }


        }
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionItem(question: Question,
                 onRatingSelected: (rating: Int) -> Unit,
                 onEditableValueChanged: (editableValue: String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.padding(vertical = 0.dp, horizontal = 10.dp)
    ) {
        Column {

            Text(text = question.questionTitle, fontSize = 25.sp)

            when (question.questionType) {
                "RATING" -> {
                    val rating = remember { mutableStateOf(0) }
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (index < rating.value) Color.Green else Color.Gray,
                                modifier = Modifier.clickable {
                                    rating.value = index + 1
                                    onRatingSelected(rating.value)
                                }
                            )
                        }
                    }
                }

                "EDITABLE" -> {
                    var text by remember { mutableStateOf("") }
                    TextField(
                        value = text,
                        onValueChange = { newText ->
                            text = newText
                            onEditableValueChanged(newText)

                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
                    )

                }
            }
        }

    }

}
@Composable
fun QuestionList(
    modifier: Modifier = Modifier,
    questionList: List<Question>,
    onNextClicked: () -> Unit,
    onQuestionResponseSubmitted: (questionNumber: String, rating: Int, editableValue: String) -> Unit,


) {
    val questionResponses by remember { mutableStateOf(mutableMapOf<String, Pair<Int, String>>()) }



    LazyColumn(modifier = modifier.padding(vertical = 30.dp)) {
        items(questionList) { question ->
            QuestionItem(question = question,
                onRatingSelected = {  rating ->
                    questionResponses[question.questionNumber] =
                        rating to (questionResponses[question.questionNumber]?.second ?: "")

                },
                onEditableValueChanged = { editableValue ->
                    questionResponses[question.questionNumber] =
                        (questionResponses[question.questionNumber]?.first ?: 0) to editableValue

                }
            )
        }
        item {
            Button(
                onClick = {
                    onNextClicked()
                    questionResponses.forEach { ( questionNumber,response) ->
                        onQuestionResponseSubmitted(questionNumber, response.first, response.second)
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 10.dp)
            ) {
                Text(text = "Submit", fontSize = 20.sp)
            }
        }
    }
}
@Composable
fun NextPage( questionList: List<Question>) {
    val questionViewModel: QuestionViewModel = viewModel()
    var questionResponses by remember { mutableStateOf(emptyList<QuestionResponse>()) }
    LaunchedEffect(Unit) {
        questionResponses = withContext(Dispatchers.IO) {
            questionViewModel.getAllQuestionResponses()
        }
        }


    LazyColumn {
        items(questionResponses) { questionResponse ->
            val questionNumber = questionResponse.questionNumber
            val questionType = getQuestionType(questionNumber,questionList)
            when (questionType) {
                "RATING" -> {
                    Text(text = "Rating: ${questionResponse.rating}",fontSize = 20.sp)
                    Log.d("QuestionResponse", "Rating: ${questionResponse.rating}")
                }
                "EDITABLE" -> {
                    Text(text = "Feedback: ${questionResponse.editableValue}",fontSize = 20.sp)
                }

            }

        }
    }
}

private fun getQuestionType(questionNumber: String, questionList: List<Question>): String{
    val question = questionList.find { it.questionNumber == questionNumber }
    return question?.questionType ?: ""

}





