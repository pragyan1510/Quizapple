package com.example.quizapple

// Kotlin ka native List use karna chahiye
import kotlin.collections.List

// Simplified Question data class
data class Question(
    var question: String,
    var options: List<String>,
    var answer: String
)

// Container class for the list of questions
data class QuestionData(
    val questions: List<Question>
)
