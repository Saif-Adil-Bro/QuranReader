with open("app/src/main/java/com/example/ui/viewmodels/PostsViewModel.kt", "r") as f:
    content = f.read()

target = """    fun addBlogPost(title: String, content: String, category: String, author: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        postsRepository.addBlogPost(title, content, category, author, onSuccess, onError)
    }"""

replacement = """    fun addBlogPost(title: String, content: String, category: String, author: String, imageUrl: String = "", onSuccess: () -> Unit, onError: (String) -> Unit) {
        postsRepository.addBlogPost(title, content, category, author, imageUrl, onSuccess, onError)
    }"""

if target in content:
    with open("app/src/main/java/com/example/ui/viewmodels/PostsViewModel.kt", "w") as f:
        f.write(content.replace(target, replacement))
    print("Success")
else:
    print("Target not found")
