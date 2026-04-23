package com.example.version.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "resetPassword"

    const val HOME = "home"
    const val SEARCH = "search"
    const val UPLOAD = "upload"

    const val EDIT_PROFILE = "edit_profile"
    const val PROFILE = "profile/{userId}"

    const val PHOTO_DETAILS = "photoDetails/{postId}"
    const val COMMENTS = "comments/{postId}"

    fun profile(userId: String) = "profile/$userId"
    fun photoDetails(postId: String) = "photoDetails/$postId"
    fun comments(postId: String) = "comments/$postId"
}