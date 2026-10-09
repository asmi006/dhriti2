package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.security.MessageDigest
import kotlin.math.abs

@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    qrColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    // Generate deterministic 21x21 QR matrix pattern from input string
    val matrix = remember(data) {
        generateQrMatrix(data, 21)
    }

    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 16.dp)) {
            val moduleCount = 21
            val moduleSize = this.size.width / moduleCount

            for (r in 0 until moduleCount) {
                for (c in 0 until moduleCount) {
                    if (matrix[r][c]) {
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(c * moduleSize, r * moduleSize),
                            size = Size(moduleSize, moduleSize)
                        )
                    }
                }
            }
        }
    }
}

private fun generateQrMatrix(input: String, size: Int): Array<BooleanArray> {
    val grid = Array(size) { BooleanArray(size) }

    // Position detection patterns at corners (7x7)
    fun drawFinderPattern(startRow: Int, startCol: Int) {
        for (r in 0..6) {
            for (c in 0..6) {
                val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                val isInner = r in 2..4 && c in 2..4
                grid[startRow + r][startCol + c] = isOuter || isInner
            }
        }
    }

    // Top-left, top-right, bottom-left
    drawFinderPattern(0, 0)
    drawFinderPattern(0, size - 7)
    drawFinderPattern(size - 7, 0)

    // Timing patterns
    for (i in 7 until size - 7) {
        grid[6][i] = i % 2 == 0
        grid[i][6] = i % 2 == 0
    }

    // Deterministic data fill based on SHA-256 hash
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(input.toByteArray())

    var hashIdx = 0
    for (r in 0 until size) {
        for (c in 0 until size) {
            // Skip finder patterns & timing
            val inTopLeft = r <= 7 && c <= 7
            val inTopRight = r <= 7 && c >= size - 8
            val inBottomLeft = r >= size - 8 && c <= 7
            val inTiming = r == 6 || c == 6

            if (!inTopLeft && !inTopRight && !inBottomLeft && !inTiming) {
                val byteVal = abs(hash[hashIdx % hash.size].toInt())
                val bitVal = (byteVal shr ((r * size + c) % 8)) and 1
                grid[r][c] = (bitVal == 1) xor ((r + c) % 3 == 0)
                hashIdx++
            }
        }
    }

    return grid
}
