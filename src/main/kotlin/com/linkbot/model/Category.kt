package com.linkbot.model

import java.util.stream.Collectors

data class Category(val keywords: List<String> = listOf()) {

    override fun toString() = keywords.stream().collect(Collectors.joining(", "))

    companion object {

        lateinit var categories: List<Category>

        fun toKeywords(chName: String): List<String> {
            var keywords = chName.split("-0-")
            keywords = keywords.map{ it.replace("-", " ") }
            return keywords
        }

        fun toChannelName(keywords: List<String>): String {
            return keywords.stream().map { it.replace(" ", "-") }.collect(Collectors.joining("-0-"))
        }

        fun categoriesFor(text: String): List<Category> {
            return categories.filter { category -> category.keywords.firstOrNull { keyword -> text.contains(keyword, true) } != null }
        }
    }
}