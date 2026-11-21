package dev.nitpick.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.Commit
import dev.nitpick.app.graph.CommitGraphRow
import dev.nitpick.app.graph.buildCommitGraph

private val LaneColors = listOf(
    Color(0xFF3B82F6),
    Color(0xFFEF4444),
    Color(0xFF10B981),
    Color(0xFFF59E0B),
    Color(0xFF8B5CF6),
    Color(0xFF06B6D4),
    Color(0xFFEC4899),
    Color(0xFF84CC16),
)

private val LaneSpacing = 18.dp
private val LaneLeftPadding = 12.dp
private val RowHeight = 52.dp
private const val CommitLimit = 500

@Composable
fun CommitGraphScreen(
    commits: List<Commit> = previewCommits,
    modifier: Modifier = Modifier,
) {
    val rows = remember(commits) { buildCommitGraph(commits.take(CommitLimit)) }
    var selectedId by remember { mutableStateOf(rows.firstOrNull()?.commit?.id) }
    val selected = rows.firstOrNull { it.commit.id == selectedId }?.commit
    val laneCount = rows.maxOfOrNull(::laneCountForRow)?.coerceAtLeast(1) ?: 1
    val graphWidth = LaneLeftPadding * 2 + LaneSpacing * laneCount

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = "Nitpick · commit graph",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Preview data — the desktop repo picker (#15) will feed real commits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            Row(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(0.62f).fillMaxHeight(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
                ) {
                    items(rows, key = { it.commit.id }) { row ->
                        CommitRow(
                            row = row,
                            graphWidth = graphWidth,
                            selected = row.commit.id == selectedId,
                            onClick = { selectedId = row.commit.id },
                        )
                    }
                }
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
                CommitDetails(
                    commit = selected,
                    modifier = Modifier.weight(0.38f).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun CommitRow(
    row: CommitGraphRow,
    graphWidth: androidx.compose.ui.unit.Dp,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommitGraphLane(
            row = row,
            modifier = Modifier.width(graphWidth).height(RowHeight),
        )
        Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
            Text(
                text = row.commit.shortMessage,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${row.commit.authorName} · ${row.commit.id.take(7)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = formatCommitTimestamp(row.commit.timestampEpochSeconds),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun CommitGraphLane(
    row: CommitGraphRow,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val laneY = size.height / 2f
        val lanes = laneCountForRow(row)
        val stroke = 2.dp.toPx()
        val dotRadius = 5.dp.toPx()
        val selectedRing = 8.dp.toPx()
        fun x(lane: Int): Float = LaneLeftPadding.toPx() + lane * LaneSpacing.toPx()

        val incoming = row.incomingLanes.toSet()
        val parents = row.parentLanes.toSet()

        // Lines that pass through the row without touching this commit.
        for (lane in 0 until lanes) {
            val before = row.lanesBefore.getOrNull(lane) != null
            val after = row.lanesAfter.getOrNull(lane) != null
            if (lane == row.lane || lane in incoming || lane in parents) continue
            when {
                before && after -> drawLine(
                    color = laneColor(lane),
                    start = Offset(x(lane), 0f),
                    end = Offset(x(lane), size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                before -> drawLine(
                    color = laneColor(lane),
                    start = Offset(x(lane), 0f),
                    end = Offset(x(lane), laneY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                after -> drawLine(
                    color = laneColor(lane),
                    start = Offset(x(lane), laneY),
                    end = Offset(x(lane), size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        }

        // Children converge into this commit.
        for (lane in row.incomingLanes) {
            drawLanePath(
                fromX = x(lane),
                fromY = 0f,
                toX = x(row.lane),
                toY = laneY,
                color = laneColor(lane),
                stroke = stroke,
            )
        }

        // This commit flows into each parent lane.
        for (lane in row.parentLanes) {
            drawLanePath(
                fromX = x(row.lane),
                fromY = laneY,
                toX = x(lane),
                toY = size.height,
                color = laneColor(lane),
                stroke = stroke,
            )
        }

        val center = Offset(x(row.lane), laneY)
        if (row.incomingLanes.isNotEmpty() || row.parentLanes.isNotEmpty()) {
            drawCircle(
                color = laneColor(row.lane).copy(alpha = 0.25f),
                radius = selectedRing,
                center = center,
            )
        }
        drawCircle(color = laneColor(row.lane), radius = dotRadius, center = center)
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = dotRadius / 2.4f,
            center = center,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLanePath(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    color: Color,
    stroke: Float,
) {
    if (fromX == toX) {
        drawLine(
            color = color,
            start = Offset(fromX, fromY),
            end = Offset(toX, toY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        return
    }
    val midY = (fromY + toY) / 2f
    val path = Path().apply {
        moveTo(fromX, fromY)
        cubicTo(fromX, midY, toX, midY, toX, toY)
    }
    drawPath(path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

@Composable
private fun CommitDetails(
    commit: Commit?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (commit == null) {
            Text("Select a commit", style = MaterialTheme.typography.titleMedium)
            return@Column
        }
        Text(
            text = commit.shortMessage,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = commit.fullMessage,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(4.dp))
        Metadata("Author", "${commit.authorName} <${commit.authorEmail}>")
        Metadata("Date", formatCommitTimestamp(commit.timestampEpochSeconds))
        Metadata("Commit", commit.id, monospace = true)
        if (commit.parentIds.isNotEmpty()) {
            Metadata("Parents", commit.parentIds.joinToString("\n") { it.take(12) }, monospace = true)
        }
    }
}

@Composable
private fun Metadata(label: String, value: String, monospace: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
        )
    }
}

private fun laneCountForRow(row: CommitGraphRow): Int {
    var maxLane = maxOf(row.lane, 0)
    row.incomingLanes.forEach { maxLane = maxOf(maxLane, it) }
    row.parentLanes.forEach { maxLane = maxOf(maxLane, it) }
    return maxOf(maxLane, row.lanesBefore.size - 1, row.lanesAfter.size - 1) + 1
}

private fun laneColor(lane: Int): Color = LaneColors[((lane % LaneColors.size) + LaneColors.size) % LaneColors.size]
