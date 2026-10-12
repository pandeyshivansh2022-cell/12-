package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

data class QuickPrompt(
    val title: String,
    val prompt: String,
    val icon: ImageVector,
    val accentColor: Color = NeonCyan
)

val defaultQuickPrompts = listOf(
    QuickPrompt(
        title = "Check-in & Connect",
        prompt = "Hey Gangstream! Let's connect. What's an inspiring thought or creative spark on your mind today?",
        icon = Icons.Default.Favorite,
        accentColor = Color(0xFFF43F5E)
    ),
    QuickPrompt(
        title = "Curiosity Spark",
        prompt = "What is a mind-bending question or scientific concept that changes how we see the world?",
        icon = Icons.Default.AutoAwesome,
        accentColor = NeonCyan
    ),
    QuickPrompt(
        title = "Neural Breakdown",
        prompt = "Provide a deep, step-by-step cognitive analysis of quantum computing and neural networks.",
        icon = Icons.Default.Psychology,
        accentColor = NeonIndigo
    ),
    QuickPrompt(
        title = "Live Tech Search",
        prompt = "Search and summarize the latest breakthrough updates in artificial intelligence from this week.",
        icon = Icons.Default.Search,
        accentColor = NeonElectricBlue
    ),
    QuickPrompt(
        title = "Cyber Engineer",
        prompt = "Review this architectural idea and help me design a resilient, modern system.",
        icon = Icons.Default.Code,
        accentColor = Color(0xFF10B981)
    )
)

@Composable
fun QuickPromptChips(
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_prompt_chips"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(defaultQuickPrompts) { item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberSurfaceElevated)
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                item.accentColor.copy(alpha = 0.45f),
                                CyberBorder
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onPromptSelected(item.prompt) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryDark
                    )
                }
            }
        }
    }
}
