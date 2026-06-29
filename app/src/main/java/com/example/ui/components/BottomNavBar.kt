package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    object Home : BottomNavItem("home", "হোম", Icons.Filled.Home, Icons.Outlined.Home)
    object Mushaf : BottomNavItem("mushaf", "মুসহাফ", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook)
    object Search : BottomNavItem("search", "সার্চ", Icons.Filled.Search, Icons.Outlined.Search)
    object Menu : BottomNavItem("settings", "মেনু", Icons.Filled.Menu, Icons.Outlined.Menu)
}

@Composable
fun BottomNavBar(
    navController: NavController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Mushaf,
        BottomNavItem.Search,
        BottomNavItem.Menu
    )

    // The bottom nav bar should only be visible on top-level screens.
    // If you have detail screens, you might want to hide it. We can handle it externally or here.
    val isBottomNavItem = items.any { it.route == currentRoute } || currentRoute == "settings"

    if (isBottomNavItem) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    if (index == 2) {
                        // Place a center play button placeholder or spacer if needed
                        // Spacer(modifier = Modifier.weight(1f))
                    }
                    val isSelected = currentRoute == item.route || (item.route == "menu" && currentRoute == "settings")
                    BottomNavIcon(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    navController.graph.startDestinationRoute?.let { route ->
                                        popUpTo(route) {
                                            saveState = true
                                        }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavIcon(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emeraldGreen = Color(0xFF10B981)
    val gray400 = Color(0xFF9CA3AF)
    val gray500 = Color(0xFF6B7280)

    val color = if (isSelected) emeraldGreen else gray500
    val iconColor = if (isSelected) emeraldGreen else gray400
    val icon = if (isSelected) item.activeIcon else item.inactiveIcon
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = item.title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.title,
            color = color,
            fontSize = 12.sp,
            fontWeight = fontWeight
        )
    }
}
