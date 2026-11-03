package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondarySlate

data class GradeStockData(
    val grade: Int,
    val gradeLabel: String,
    val stage: String,
    val totalStock: Int
)

data class SubjectStockData(
    val subject: String,
    val totalStock: Int,
    val color: Color
)

private val ChartPalette = listOf(
    Color(0xFF006F88), // Primary Teal
    Color(0xFF0D9488), // Forest Green
    Color(0xFF0284C7), // Sky Blue
    Color(0xFF7C3AED), // Purple Accent
    Color(0xFFD97706), // Amber
    Color(0xFFE11D48), // Rose
    Color(0xFF475569), // Slate
    Color(0xFF059669)  // Emerald
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StockDistributionCard(
    books: List<Book>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Grades (Bar Chart), 1 = Subjects (Donut Chart)

    // Aggregate by Grade
    val gradeData = remember(books) {
        val gradeMap = mutableMapOf<Int, Int>()
        for (b in books) {
            gradeMap[b.grade] = (gradeMap[b.grade] ?: 0) + b.currentStock
        }
        (1..12).map { grade ->
            val stage = when {
                grade <= 6 -> "ابتدائي"
                grade <= 9 -> "إعدادي"
                else -> "ثانوي"
            }
            GradeStockData(
                grade = grade,
                gradeLabel = "ص$grade",
                stage = stage,
                totalStock = gradeMap[grade] ?: 0
            )
        }.filter { it.totalStock > 0 }
    }

    // Aggregate by Subject
    val subjectData = remember(books) {
        val subjectMap = mutableMapOf<String, Int>()
        for (b in books) {
            val key = b.subject.trim().ifBlank { "عام" }
            subjectMap[key] = (subjectMap[key] ?: 0) + b.currentStock
        }
        subjectMap.entries.sortedByDescending { it.value }.mapIndexed { index, entry ->
            SubjectStockData(
                subject = entry.key,
                totalStock = entry.value,
                color = ChartPalette[index % ChartPalette.size]
            )
        }
    }

    val totalCopies = remember(books) { books.sumOf { it.currentStock } }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stock_distribution_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryTeal.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.BarChart else Icons.Default.PieChart,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تحليل توزيع المخزون الفعلي (Stock Distribution)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "رؤية بصرية تفاعلية للأرصدة عبر الصفوف والمواد الدراسية",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryTeal.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$totalCopies نسخة",
                        color = PrimaryTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switcher Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 0 }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selectedTab == 0) PrimaryTeal else MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "حسب الصفوف (Bar Chart)",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 1 }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selectedTab == 1) PrimaryTeal else MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "حسب المواد (Donut Chart)",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Content
            if (selectedTab == 0) {
                StockBarChart(
                    data = gradeData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                )
            } else {
                StockDonutChart(
                    data = subjectData,
                    totalCopies = totalCopies,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Subject Legend
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjectData.forEach { item ->
                        val percent = if (totalCopies > 0) (item.totalStock * 100 / totalCopies) else 0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(item.color)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${item.subject}: ${item.totalStock} ($percent%)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Compose Bar Chart rendering stock across educational grades.
 */
@Composable
fun StockBarChart(
    data: List<GradeStockData>,
    modifier: Modifier = Modifier
) {
    var highlightedIndex by remember { mutableStateOf<Int?>(null) }
    val maxStock = remember(data) { (data.maxOfOrNull { it.totalStock } ?: 100).coerceAtLeast(1) }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    val primaryColor = PrimaryTeal
    val highlightColor = Color(0xFF0D9488)
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier) {
        // Selected bar info tooltip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (highlightedIndex != null && highlightedIndex in data.indices) {
                val item = data[highlightedIndex!!]
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "${item.stage} - الصف ${item.grade}: ${item.totalStock} نسخة بالمستودع",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = "انقر على أي عمود لاستعراض رصيد الصف بدقة",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Custom Canvas Bar Rendering
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val count = data.size
                        if (count > 0) {
                            val slotWidth = size.width / count
                            val clickedIndex = (offset.x / slotWidth).toInt().coerceIn(0, count - 1)
                            highlightedIndex = if (highlightedIndex == clickedIndex) null else clickedIndex
                        }
                    }
                }
        ) {
            val count = data.size
            if (count == 0) return@Canvas

            val availableWidth = size.width
            val availableHeight = size.height - 24.dp.toPx()
            val slotWidth = availableWidth / count
            val barWidth = (slotWidth * 0.62f).coerceAtMost(36.dp.toPx())

            // Baseline
            drawLine(
                color = surfaceVariant,
                start = Offset(0f, availableHeight),
                end = Offset(availableWidth, availableHeight),
                strokeWidth = 2.dp.toPx()
            )

            data.forEachIndexed { index, item ->
                val isSelected = highlightedIndex == index
                val fraction = (item.totalStock.toFloat() / maxStock) * animationProgress.value
                val barHeight = availableHeight * fraction
                val left = (index * slotWidth) + (slotWidth - barWidth) / 2f
                val top = availableHeight - barHeight

                val barColor = if (isSelected) highlightColor else primaryColor
                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        barColor,
                        barColor.copy(alpha = if (isSelected) 0.95f else 0.75f)
                    ),
                    startY = top,
                    endY = availableHeight
                )

                // Bar
                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        // Labels row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEachIndexed { index, item ->
                val isSelected = highlightedIndex == index
                Text(
                    text = item.gradeLabel,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Interactive Compose Donut Chart rendering subject distribution.
 */
@Composable
fun StockDonutChart(
    data: List<SubjectStockData>,
    totalCopies: Int,
    modifier: Modifier = Modifier
) {
    var highlightedIndex by remember { mutableStateOf<Int?>(null) }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(data) {
                    detectTapGestures {
                        // Toggle selected slice
                        highlightedIndex = if (highlightedIndex == null) 0 else null
                    }
                }
        ) {
            val total = if (totalCopies > 0) totalCopies.toFloat() else 1f
            val diameter = size.minDimension.coerceAtMost(160.dp.toPx())
            val strokeWidth = 24.dp.toPx()
            val radius = (diameter - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            var startAngle = -90f

            data.forEachIndexed { index, item ->
                val sweep = (item.totalStock / total) * 360f * animationProgress.value
                val isSelected = highlightedIndex == index
                val currentStroke = if (isSelected) strokeWidth * 1.25f else strokeWidth

                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = currentStroke, cap = StrokeCap.Round)
                )

                startAngle += sweep
            }
        }

        // Central Counter Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$totalCopies",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "نسخة إجمالية",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
