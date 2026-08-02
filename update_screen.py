with open("app/src/main/java/com/example/ui/screens/PostsScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports_target = "import androidx.compose.ui.platform.LocalContext"
imports_replacement = """import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale"""

content = content.replace(imports_target, imports_replacement, 1)

# Update BlogPostCard
card_target = """            Text(
                text = post.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))"""

card_replacement = """            Text(
                text = post.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (post.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "কভার ফটো",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))"""

content = content.replace(card_target, card_replacement, 1)

# Update BlogPostDetailScreen
detail_target = """            // Post Title
            Text(
                text = post.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 30.sp
            )
            Spacer(modifier = Modifier.height(16.dp))"""

detail_replacement = """            // Post Title
            Text(
                text = post.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 30.sp
            )
            if (post.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "কভার ফটো",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))"""

content = content.replace(detail_target, detail_replacement, 1)

# Update AddPostDialog variables
dialog_vars_target = """    var title by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }"""

dialog_vars_replacement = """    var title by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }"""

content = content.replace(dialog_vars_target, dialog_vars_replacement, 1)

# Update AddPostDialog inputs
dialog_input_target = """                    if (isBlogType) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("ব্লগ শিরোনাম") },
                            placeholder = { Text("যেমন: ফজরের নামাজের গুরুত্ব ও ফজিলত") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Title,
                                    contentDescription = null,
                                    tint = PrimaryGreen
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }"""

dialog_input_replacement = """                    if (isBlogType) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("ব্লগ শিরোনাম") },
                            placeholder = { Text("যেমন: ফজরের নামাজের গুরুত্ব ও ফজিলত") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Title,
                                    contentDescription = null,
                                    tint = PrimaryGreen
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("কভার ফোটো লিঙ্ক (Image URL)") },
                            placeholder = { Text("যেমন: https://example.com/image.jpg (ঐচ্ছিক)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = PrimaryGreen
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }"""

content = content.replace(dialog_input_target, dialog_input_replacement, 1)

# Update AddPostDialog submit call
submit_target = """                                viewModel.addBlogPost(
                                    title = title,
                                    content = contentText,
                                    category = category,
                                    author = author,"""

submit_replacement = """                                viewModel.addBlogPost(
                                    title = title,
                                    content = contentText,
                                    category = category,
                                    author = author,
                                    imageUrl = imageUrl,"""

content = content.replace(submit_target, submit_replacement, 1)

with open("app/src/main/java/com/example/ui/screens/PostsScreen.kt", "w") as f:
    f.write(content)

print("PostsScreen updated successfully!")
