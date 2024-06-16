package com.example.quizapple

import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var  questiontextview : TextView
    private lateinit var optionRadioGroup: RadioGroup
    private lateinit var nextButton: Button
    private lateinit var timerTextView: TextView


    private var questionList = mutableListOf<Question>()
    private var currentquestionIndex = 0

    private lateinit var countDownTimer: CountDownTimer
    private var timeleftinMillis : Long = 600000 // 10 min counter in milliseconds

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "Quizpreference"
    private val KEY_CURRENT_QUESTION_INDEX = "currentquestionindex"









    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        questiontextview = findViewById(R.id.questionTextView)
        optionRadioGroup = findViewById(R.id.optionsRadioGroup)
        nextButton = findViewById(R.id.nextButton)
        timerTextView = findViewById(R.id.timerTextView)



        // Load questions from JSON file
        LoadQuestionFromJSON()

        // Initialize timer if needed
        initializeTimer()

        // Display the first question
        Displayquestion(currentquestionIndex)

        //next question button
        nextButton.setOnClickListener {
            onNextButtonClick(it)
        }
    }

    private fun initializeViews() {
        questiontextview = findViewById(R.id.questionTextView)
        optionRadioGroup = findViewById(R.id.optionsRadioGroup)
        nextButton = findViewById(R.id.nextButton)
        timerTextView = findViewById(R.id.timerTextView)
    }

    private fun LoadQuestionFromJSON() {
        val json: String? = loadJsonFromAsset("questions.json")
        if (!json.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<QuestionData>() {}.type
            val questionData: QuestionData = gson.fromJson(json, type)
            questionList = questionData.questions.toMutableList()
        } else {
            // Handle case where JSON couldn't be loaded
            Toast.makeText(this, "Error loading questions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadJsonFromAsset(filename: String): String? {
        var json: String? = null
        try {
            val inputStream = assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.defaultCharset())
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error reading JSON file", Toast.LENGTH_SHORT).show()
        }
        return json
    }

    fun Displayquestion(index : Int) {

        if(index<questionList.size) {
            val question = questionList[index]
            questiontextview.text = question.question


            optionRadioGroup.removeAllViews()
            for (option in question.options) {
                val radioButton = RadioButton(this)
                radioButton.text = option
                optionRadioGroup.addView(radioButton)
            }
        }else{
            Toast.makeText(this, "No more questions", Toast.LENGTH_SHORT).show()
        }
    }

         private fun initializeTimer() {
            countDownTimer = object : CountDownTimer(timeleftinMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeleftinMillis = millisUntilFinished
                    // Update timer display if needed
                    val minutes = (timeleftinMillis/1000)/60
                    val seconds = (timeleftinMillis/1000)%60
                    timerTextView.text = String.format("%02d:%02d",minutes,seconds)
                }

                override fun onFinish() {
                    // Handle quiz completion due to timeout
                    Toast.makeText(this@MainActivity, "Time's up!", Toast.LENGTH_SHORT).show()
                }
            }.start()
        }

    private fun restorequiz(){
        currentquestionIndex = sharedPreferences.getInt(KEY_CURRENT_QUESTION_INDEX,0)
    }

    private fun savequizstate(){
        sharedPreferences.edit().putInt(KEY_CURRENT_QUESTION_INDEX,currentquestionIndex).apply()
    }

    private fun onNextButtonClick(view: View) {
        val selectedoptionid = optionRadioGroup.checkedRadioButtonId
        if(selectedoptionid != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedoptionid)
            val selectedanswer = selectedRadioButton.text.toString()
            val correctanswer = questionList[currentquestionIndex].answer

            if (selectedanswer == correctanswer) {
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Incorrect!!", Toast.LENGTH_SHORT).show()
            }

            //moving to the next question
            if (currentquestionIndex < questionList.size - 1) {
                currentquestionIndex++
                Displayquestion(currentquestionIndex)
            } else {
                Toast.makeText(this, "You have completed the quiz!!", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "Please select an option!", Toast.LENGTH_SHORT).show()

        }
    }
    override fun onPause() {
        super.onPause()
        savequizstate() // Save quiz state when activity is paused
        countDownTimer.cancel() // Cancel the countdown timer
    }

    override fun onResume() {
        super.onResume()
        initializeTimer() // Re-initialize the timer when activity is resumed
    }
}