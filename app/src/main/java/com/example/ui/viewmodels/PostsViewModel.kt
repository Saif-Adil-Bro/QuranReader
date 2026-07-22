package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BlogPost
import com.example.data.model.ShortPost
import com.example.data.repository.PostsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PostsViewModel(private val postsRepository: PostsRepository) : ViewModel() {

    val rawBlogPosts = postsRepository.blogPosts
    val rawShortPosts = postsRepository.shortPosts
    val isLoading = postsRepository.isLoading

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("সকল")

    val filteredBlogPosts: StateFlow<List<BlogPost>> = combine(
        rawBlogPosts,
        searchQuery,
        selectedCategory
    ) { posts, query, category ->
        posts.filter { post ->
            val matchesCategory = (category == "সকল" || post.category == category)
            val matchesQuery = query.isEmpty() ||
                    post.title.contains(query, ignoreCase = true) ||
                    post.content.contains(query, ignoreCase = true) ||
                    post.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredShortPosts: StateFlow<List<ShortPost>> = combine(
        rawShortPosts,
        searchQuery,
        selectedCategory
    ) { posts, query, category ->
        posts.filter { post ->
            val matchesCategory = (category == "সকল" || post.category == category)
            val matchesQuery = query.isEmpty() ||
                    post.text.contains(query, ignoreCase = true) ||
                    post.reference.contains(query, ignoreCase = true) ||
                    post.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBlogPost(title: String, content: String, category: String, author: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        postsRepository.addBlogPost(title, content, category, author, onSuccess, onError)
    }

    fun addShortPost(text: String, reference: String, category: String, author: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        postsRepository.addShortPost(text, reference, category, author, onSuccess, onError)
    }
}
