package dev.nitpick.app.domain

enum class RefType { LOCAL_BRANCH, REMOTE_BRANCH, TAG }

data class Ref(
    val name: String,
    val fullName: String,
    val commitId: String,
    val type: RefType,
)
