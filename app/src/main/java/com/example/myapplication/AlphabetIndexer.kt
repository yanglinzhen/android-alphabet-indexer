package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

@Composable
fun AlphaIndex() {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var yOffset by remember {
            mutableFloatStateOf(0f)
        }
        val chars = ('A'..'Z').plus("#").toList()
        val rects = mutableMapOf<Int, IntRange>()

        var highlightIndex by remember {
            mutableIntStateOf(-1)
        }
        AlphabetIndexer(
            content = {
                chars
                    .mapIndexed { index, char ->
                        Text(
                            fontSize = 32.sp,
                            text = char.toString(),
                            color = if (highlightIndex == index) Color.Red else Color.Black,
                            modifier = Modifier
                                .onPlaced {
                                    it
                                        .boundsInParent()
                                        .let { rect ->
                                            rect.top to rect.bottom
                                        }
                                        .also { pair ->
                                            rects[index] =
                                                IntRange(pair.first.toInt(), pair.second.toInt())
                                        }
                                }
                        )
                    }
            },
            modifier = Modifier
                .heightIn(
                    max = 590.dp
                )
                .background(Color.Cyan)
        ) {
            yOffset = it
            if (yOffset > 0) {
                rects.entries.filter { entry ->
                    yOffset.toInt() in entry.value
                }.minByOrNull { entry ->
                    val avg = entry.value.average()
                    (avg.toInt() - yOffset).absoluteValue
                }?.also { entry ->
                    highlightIndex = entry.key
                    Log.d(
                        "TAG", "AlphaIndex: ${
                            chars[highlightIndex]
                        } are touched!"
                    )
                }
            } else {
                highlightIndex = -1
            }
        }
    }
}

@Composable
fun AlphabetIndexer(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    yPositionListener: (Float) -> Unit = {},
) {
    val internalModifier = Modifier
        .padding(8.dp)
        .pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragStart = {
                    yPositionListener.invoke(it.y)
                },
                onDragEnd = {
                    yPositionListener.invoke(-1f)
                },
                onDragCancel = {
                    yPositionListener.invoke(-1f)
                },
                onVerticalDrag = { change, dragAmount ->
                    yPositionListener.invoke(change.position.y)
                }
            )
        }

    Layout(
        content = content, modifier = modifier
            .then(internalModifier)
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        val maxWidth = placeables.maxOf {
            it.width
        }

        val yOffset = constraints.maxHeight / placeables.size

        layout(
            maxWidth,
            constraints.maxHeight
        ) {
            placeables.forEachIndexed { index, placeable ->
                val xOffset = (maxWidth - placeable.width) / 2
                placeable.place(
                    x = xOffset,
                    y = index * yOffset
                )
            }
        }
    }
}