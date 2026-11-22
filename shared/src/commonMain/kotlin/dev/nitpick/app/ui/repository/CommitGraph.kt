package dev.nitpick.app.ui.repository

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.Commit
import dev.nitpick.app.graph.CommitGraphRow
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val LaneSpacing = 16.dp
private val LaneLeftPadding = 10.dp
private val RowHeight = 40.dp

/**
 * The commit list drawn as a graph: one lane and one color per line of history. [showLanes] is
 * false for a filtered subset, whose lanes would point at rows that are no longer there.
 */
@Composable
fun CommitGraphList(
    graph: List<CommitGraphRow>,
    selectedCommitId: String?,
    onSelectCommit: (String) -> Unit,
    modifier: Modifier = Modifier,
    showLanes: Boolean = true,
    compactTime: Boolean = false,
) {
    val laneCount = remember(graph) { graph.maxOfOrNull(::laneCountForRow)?.coerceAtLeast(1) ?: 1 }
    val graphWidth = if (showLanes) LaneLeftPadding * 2 + LaneSpacing * laneCount else LaneLeftPadding

    LazyColumn(modifier, contentPadding = PaddingValues(vertical = 4.dp)) {
        items(graph, key = { it.commit.id }) { row ->
            CommitGraphRowItem(
                row = row,
                graphWidth = graphWidth,
                showLanes = showLanes,
                compactTime = compactTime,
                selected = row.commit.id == selectedCommitId,
                onClick = { onSelectCommit(row.commit.id) },
            )
        }
    }
}

@Composable
private fun CommitGraphRowItem(row: CommitGraphRow, graphWidth: Dp, showLanes: Boolean, compactTime: Boolean, selected: Boolean, onClick: () -> Unit) {
    val time = remember(row.commit.timestampEpochSeconds, compactTime) {
        if (compactTime) formatCommitTimeCompact(row.commit.timestampEpochSeconds) else formatCommitTime(row.commit.timestampEpochSeconds)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) NitpickTheme.colors.selection else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showLanes) CommitGraphLane(row, Modifier.width(graphWidth).height(RowHeight)) else Spacer(Modifier.width(graphWidth).height(RowHeight))
        Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
            Text(
                text = row.commit.shortMessage,
                style = NitpickTheme.typography.body,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                color = if (selected) NitpickTheme.colors.onSelection else NitpickTheme.colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${row.commit.authorName} · ${row.commit.id.take(7)}",
                style = NitpickTheme.typography.codeSmall,
                color = if (selected) NitpickTheme.colors.onSelection.copy(alpha = 0.72f) else NitpickTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = time,
            style = NitpickTheme.typography.codeSmall,
            color = NitpickTheme.colors.textMuted,
        )
    }
}

@Composable
private fun CommitGraphLane(row: CommitGraphRow, modifier: Modifier) {
    val palette = NitpickTheme.colors.lanePalette
    val passThrough = remember(row) { passThroughLanes(row) }
    Canvas(modifier) {
        val laneY = size.height / 2f
        val stroke = 2.dp.toPx()
        val dotRadius = 4.dp.toPx()
        val selectedRing = 7.dp.toPx()
        fun x(lane: Int): Float = LaneLeftPadding.toPx() + lane * LaneSpacing.toPx()

        for (lane in passThrough) {
            val before = row.lanesBefore.getOrNull(lane) != null
            val after = row.lanesAfter.getOrNull(lane) != null
            val top = if (before) 0f else laneY
            val bottom = if (after) size.height else laneY
            drawLine(laneColor(lane, palette), Offset(x(lane), top), Offset(x(lane), bottom), stroke, StrokeCap.Round)
        }

        // Children converge into this commit, which then flows into each parent lane.
        row.incomingLanes.forEach { lane -> drawLanePath(x(lane), 0f, x(row.lane), laneY, laneColor(lane, palette), stroke) }
        row.parentLanes.forEach { lane -> drawLanePath(x(row.lane), laneY, x(lane), size.height, laneColor(lane, palette), stroke) }

        val center = Offset(x(row.lane), laneY)
        if (row.incomingLanes.isNotEmpty() || row.parentLanes.isNotEmpty()) {
            drawCircle(laneColor(row.lane, palette).copy(alpha = 0.22f), selectedRing, center)
        }
        drawCircle(laneColor(row.lane, palette), dotRadius, center)
        drawCircle(Color.White.copy(alpha = 0.75f), dotRadius / 2.4f, center)
    }
}

private fun DrawScope.drawLanePath(fromX: Float, fromY: Float, toX: Float, toY: Float, color: Color, stroke: Float) {
    if (fromX == toX) {
        drawLine(color, Offset(fromX, fromY), Offset(toX, toY), stroke, StrokeCap.Round)
        return
    }
    val midY = (fromY + toY) / 2f
    val path = Path().apply {
        moveTo(fromX, fromY)
        cubicTo(fromX, midY, toX, midY, toX, toY)
    }
    drawPath(path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

/** Full message, author, date, id and parents of the selected commit. */
@Composable
fun CommitDetails(commit: Commit, modifier: Modifier = Modifier) {
    // Git's subject is the whole first paragraph, already shown as the title.
    val body = remember(commit.fullMessage) { commit.fullMessage.substringAfter("\n\n", "").trim() }
    val time = remember(commit.timestampEpochSeconds) { formatCommitTime(commit.timestampEpochSeconds) }
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = commit.shortMessage,
            style = NitpickTheme.typography.section,
            color = NitpickTheme.colors.text,
        )
        if (body.isNotEmpty()) {
            Text(text = body, style = NitpickTheme.typography.body, color = NitpickTheme.colors.text)
        }
        Metadata(stringResource(Res.string.details_author), "${commit.authorName} <${commit.authorEmail}>")
        Metadata(stringResource(Res.string.details_date), time)
        Metadata(stringResource(Res.string.details_commit), commit.id, monospace = true)
        if (commit.parentIds.isNotEmpty()) {
            Metadata(
                stringResource(Res.string.details_parents),
                commit.parentIds.joinToString("\n") { it.take(12) },
                monospace = true,
            )
        }
    }
}

@Composable
private fun Metadata(label: String, value: String, monospace: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label.uppercase(),
            style = NitpickTheme.typography.kicker,
            color = NitpickTheme.colors.textMuted,
        )
        Text(
            text = value,
            style = if (monospace) NitpickTheme.typography.codeSmall else NitpickTheme.typography.small,
            color = NitpickTheme.colors.text,
        )
    }
}

/** Lanes whose line crosses this row without touching the commit's dot. */
internal fun passThroughLanes(row: CommitGraphRow): List<Int> =
    (0 until laneCountForRow(row)).filter { lane ->
        val before = row.lanesBefore.getOrNull(lane) != null
        val after = row.lanesAfter.getOrNull(lane) != null
        // A parent lane that opens here is drawn by the curve from the dot; one that already
        // carried a line from above keeps that line through the whole row.
        val opensHere = lane in row.parentLanes && !before
        lane != row.lane && lane !in row.incomingLanes && !opensHere && (before || after)
    }

private fun laneCountForRow(row: CommitGraphRow): Int {
    val maxLane = (listOf(row.lane) + row.incomingLanes + row.parentLanes).max()
    return maxOf(maxLane, row.lanesBefore.size - 1, row.lanesAfter.size - 1) + 1
}

private fun laneColor(lane: Int, palette: List<Color>): Color = palette[lane % palette.size]
