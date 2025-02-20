package com.example.hw32

import android.content.Context
import android.os.Bundle
import android.util.Xml
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hw32.ui.theme.Hw32Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Hw32Theme {
                val context = LocalContext.current

                // Parse the XML file to retrieve flashcards data
                var flashcards by remember { mutableStateOf(parseFlashcardsXml(context)) }

                // Launch a coroutine to shuffle the flashcards every 15 seconds
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(15_000)
                        flashcards = flashcards.shuffled()
                    }
                }

                // Set the background color of the Surface to red
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Red
                ) {
                    // Center the LazyRow in the screen using a Column
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,   // Vertically center the content
                        horizontalAlignment = Alignment.CenterHorizontally // Horizontally center the content
                    ) {
                        FlashcardRow(flashcards)
                    }
                }
            }
        }
    }
}

// Data class representing a flashcard with a question and an answer
data class Flashcard(val question: String, val answer: String)

// Function to parse the flashcards XML file (res/xml/flashcards.xml) and return a list of Flashcard objects
fun parseFlashcardsXml(context: Context): List<Flashcard> {
    val flashcards = mutableListOf<Flashcard>()
    try {
        // Obtain an XmlResourceParser instance for the flashcards XML file
        val parser = context.resources.getXml(R.xml.flashcards)
        var eventType = parser.eventType
        var question: String? = null
        var answer: String? = null

        // Iterate over the XML document until the end is reached
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    // Process start tags
                    when (parser.name) {
                        "card" -> {
                            // Reset question and answer for each new flashcard element
                            question = null
                            answer = null
                        }
                        "question" -> {
                            // Retrieve text content for the question
                            question = parser.nextText()
                        }
                        "answer" -> {
                            // Retrieve text content for the answer
                            answer = parser.nextText()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    // When reaching the end of a flashcard element, add it to the list if both fields are present
                    if (parser.name == "card") {
                        if (!question.isNullOrEmpty() && !answer.isNullOrEmpty()) {
                            flashcards.add(Flashcard(question, answer))
                        }
                    }
                }
            }
            // Move to the next XML event
            eventType = parser.next()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return flashcards
}

@Composable
fun FlashcardItem(flashcard: Flashcard) {
    // Animatable value to control the card's Y-axis rotation for the flip effect
    val rotationY = remember { Animatable(0f) }
    // State to track if the card is flipped or not
    var isFlipped by remember { mutableStateOf(false) }

    // Launch a flip animation whenever the isFlipped state changes
    LaunchedEffect(isFlipped) {
        val targetRotation = if (isFlipped) 180f else 0f
        rotationY.animateTo(
            targetValue = targetRotation,
            animationSpec = tween(durationMillis = 500)
        )
    }

    // Function to toggle the flip state of the card
    fun flipCard() {
        isFlipped = !isFlipped
    }

    // Determine whether the front side of the card is visible based on the current rotation
    val isFrontVisible = rotationY.value < 90f

    // Define the card UI with fixed width and height and padding
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(150.dp)
            .padding(8.dp)
            .clickable { flipCard() }
            .graphicsLayer {
                // Uncomment the following line to enable the 3D flip effect by applying rotationY
                // rotationY = rotationY.value
                // Adjust the camera distance to give a realistic 3D effect
                cameraDistance = 8 * density
            }
    ) {
        // Box to stack the text on top of each other and center the content
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Display the question on the front side, and the answer on the back side
            if (isFrontVisible) {
                Text(text = flashcard.question)
            } else {
                Text(text = flashcard.answer)
            }
        }
    }
}

@Composable
fun FlashcardRow(flashcards: List<Flashcard>) {
    // Create a horizontally scrolling list (LazyRow) for displaying flashcards
    LazyRow(
        horizontalArrangement = Arrangement.Center, // Center the content horizontally
        verticalAlignment = Alignment.CenterVertically // Center the content vertically
    ) {
        // Display each flashcard using the FlashcardItem composable
        items(flashcards) { card ->
            FlashcardItem(card)
        }
    }
}
