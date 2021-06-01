package com.maikandan.mathgame

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.divyanshu.draw.widget.DrawView
import java.util.Random
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var drawView: DrawView? = null
    private var clearButton: Button? = null
    private var detectButton: ImageButton? = null
    private var predictedTextView: TextView? = null
    private var firstNumber: TextView? = null
    private var secondNumber: TextView? = null
    private var digitClassifier = DigitClassifier(this)

    private var randomArray = Array<Int>(2){0}
    private var randomNumber1: Int = 0
    private var randomNumber2: Int = 0
    private var sumOfRandom: Int = 0



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Setup view instances.
        drawView = findViewById(R.id.draw_view)
        drawView?.setStrokeWidth(50.0f)
        drawView?.setColor(Color.WHITE)
        drawView?.setBackgroundColor(Color.BLACK)
        clearButton = findViewById(R.id.clear_button)
        detectButton = findViewById(R.id.detect)
        predictedTextView = findViewById(R.id.predicted_text)
        firstNumber = findViewById(R.id.firstNumber)
        secondNumber = findViewById(R.id.secondNumber)

        generateRandom()

        randomNumber1 = randomArray[0]
        randomNumber2 = randomArray[1]

        sumOfRandom = randomNumber1 + randomNumber2

        firstNumber?.text = randomNumber1.toString()
        secondNumber?.text = randomNumber2.toString()


//     Setup clear drawing button.
        clearButton?.setOnClickListener {
            drawView?.clearCanvas()
            predictedTextView?.text = getString(R.string.prediction_text_placeholder)
            digitClassifier.isInitialized = false
        }

        detectButton?.setOnClickListener {
            classifyDrawing()

            Handler().postDelayed({

                if (predictedTextView?.text.toString() == sumOfRandom.toString()) {

                    generateRandom()
                    randomNumber1 = randomArray[0]
                    randomNumber2 = randomArray[1]

                    sumOfRandom = randomNumber1 + randomNumber2

                    firstNumber?.text = randomNumber1.toString()
                    secondNumber?.text = randomNumber2.toString()

                    drawView?.clearCanvas()
                    predictedTextView?.text = getString(R.string.prediction_text_placeholder)
                    digitClassifier.isInitialized = false

                } else {

                    if (digitClassifier.isInitialized) {

                        Toast.makeText(this, "Oops! Check your calculations and try writing the number neater next time!", Toast.LENGTH_SHORT).show()

                    } else {

                        Toast.makeText(this, "Draw a number", Toast.LENGTH_SHORT).show()
                    }
                }

            }, 150)

        }

        // Setup classification trigger so that it classify after every stroke drew.
        drawView?.setOnTouchListener { _, event ->
            // As we have interrupted DrawView's touch event,
            // we first need to pass touch events through to the instance for the drawing to show up.
            drawView?.onTouchEvent(event)

            // Then if user finished a touch event, run classification
            if (event.action == MotionEvent.ACTION_UP) {
                digitClassifier.isInitialized = true
            }

            true
        }

        // Setup digit classifier.
        digitClassifier
            .initialize()
            .addOnFailureListener { e -> Log.e(TAG, "Error to setting up digit classifier.", e) }
    }

    override fun onDestroy() {
        // Sync DigitClassifier instance lifecycle with MainActivity lifecycle,
        // and free up resources (e.g. TF Lite instance) once the activity is destroyed.
        digitClassifier.close()
        super.onDestroy()
    }

    private fun classifyDrawing() {
        val bitmap = drawView?.getBitmap()

        if ((bitmap != null) && (digitClassifier.isInitialized)) {
            digitClassifier
                .classifyAsync(bitmap)
                .addOnSuccessListener { resultText -> predictedTextView?.text = resultText }
                .addOnFailureListener { e ->
                    predictedTextView?.text = getString(
                        R.string.classification_error_message,
                        e.localizedMessage
                    )
                    Log.e(TAG, "Error classifying drawing.", e)
                }
        }

    }

    private fun generateRandom(){
        val rnds1 = Random().nextInt(5)
        var rnds2 = Random().nextInt(6)

        randomArray[0] = rnds1
        randomArray[1] = rnds2
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
