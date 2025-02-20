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

                // 解析 XML
                var flashcards by remember { mutableStateOf(parseFlashcardsXml(context)) }

                // 每 15 秒随机打乱卡片顺序
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(15_000)
                        flashcards = flashcards.shuffled()
                    }
                }

                // 将背景色改为红色
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Red // 修改此处即可
                ) {
                    // 用 Column 将 LazyRow 居中
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,   // 垂直居中
                        horizontalAlignment = Alignment.CenterHorizontally // 水平居中
                    ) {
                        FlashcardRow(flashcards)
                    }
                }
            }
        }
    }
}

// 数据类
data class Flashcard(val question: String, val answer: String)

// 解析 flashcards.xml
fun parseFlashcardsXml(context: Context): List<Flashcard> {
    val flashcards = mutableListOf<Flashcard>()
    try {
        val parser = context.resources.getXml(R.xml.flashcards)
        var eventType = parser.eventType
        var question: String? = null
        var answer: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "card" -> {
                            question = null
                            answer = null
                        }
                        "question" -> {
                            question = parser.nextText()
                        }
                        "answer" -> {
                            answer = parser.nextText()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "card") {
                        if (!question.isNullOrEmpty() && !answer.isNullOrEmpty()) {
                            flashcards.add(Flashcard(question, answer))
                        }
                    }
                }
            }
            eventType = parser.next()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return flashcards
}

@Composable
fun FlashcardItem(flashcard: Flashcard) {
    // 控制翻转状态：0f 表示正面，180f 表示背面
    val rotationY = remember { Animatable(0f) }
    var isFlipped by remember { mutableStateOf(false) }

    // 当 isFlipped 改变时，执行翻转动画
    LaunchedEffect(isFlipped) {
        val targetRotation = if (isFlipped) 180f else 0f
        rotationY.animateTo(
            targetValue = targetRotation,
            animationSpec = tween(durationMillis = 500)
        )
    }

    // 点击卡片时，切换 isFlipped
    fun flipCard() {
        isFlipped = !isFlipped
    }

    val isFrontVisible = rotationY.value < 90f

    Card(
        modifier = Modifier
            .width(250.dp)
            .height(150.dp)
            .padding(8.dp)
            .clickable { flipCard() }
            .graphicsLayer {
                // 若想启用 3D 翻转，可打开下面这行：
//                rotationY = rotationY.value
                cameraDistance = 8 * density
            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
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
    // 这是一个可以横向滑动的列表
    LazyRow(
        horizontalArrangement = Arrangement.Center, // 横向内容居中
        verticalAlignment = Alignment.CenterVertically // 纵向内容居中
    ) {
        items(flashcards) { card ->
            FlashcardItem(card)
        }
    }
}
