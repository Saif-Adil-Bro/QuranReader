import re

with open("app/src/main/java/com/example/ui/screens/mushaf/MushafSelectionScreen.kt", "r") as f:
    content = f.read()

# Add imports for LazyVerticalGrid
if "import androidx.compose.foundation.lazy.grid." not in content:
    imports = """
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
"""
    content = content.replace("import androidx.compose.foundation.lazy.items\n", "import androidx.compose.foundation.lazy.items\n" + imports)

# Replace LazyColumn with LazyVerticalGrid
old_lazy_column = """                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {"""

new_lazy_grid = """                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {"""

content = content.replace(old_lazy_column, new_lazy_grid)

# Replace item { with item(span = { GridItemSpan(maxLineSpan) }) {
content = content.replace("item {", "item(span = { GridItemSpan(maxLineSpan) }) {")

with open("app/src/main/java/com/example/ui/screens/mushaf/MushafSelectionScreen.kt", "w") as f:
    f.write(content)
