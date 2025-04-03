package com.accessibilityyoutube

import androidx.compose.runtime.mutableStateOf

object SharedState {
    var playerState: PlayerState = PlayerState.NOT_RUNNING
    val sleepBeforePlayTime = mutableStateOf("3")
    val playTime = mutableStateOf("5")
    val sleepAfterPlayTime = mutableStateOf("5")

    private val appleYoutubeVideos = listOf(
        "https://www.youtube.com/watch?v=eDqfg_LexCQ",
        "https://www.youtube.com/watch?v=G0cmfY7qdmY",
        "https://www.youtube.com/watch?v=1r3_pPkjOdg",
        "https://www.youtube.com/watch?v=eYJcUtVIB_g",
        "https://www.youtube.com/watch?v=PugKQZHPut8",
        "https://www.youtube.com/watch?v=zXJbdtxh0XE",
        "https://www.youtube.com/watch?v=U1unmE6OlYM",
        "https://www.youtube.com/watch?v=Vb0dG-2huJE",
        "https://www.youtube.com/watch?v=asKvPLmjxXY",
        "https://www.youtube.com/watch?v=TX9qSaGXFyg",
        "https://www.youtube.com/watch?v=fOHj5kGU4fY",
        "https://www.youtube.com/watch?v=ovC63II8ofQ",
        "https://www.youtube.com/watch?v=otQF0wkvuwI",
        "https://www.youtube.com/watch?v=nI9hCFnM_6Y"
    )

    fun getRandomYoutubeUrl(): String {
        return appleYoutubeVideos.random()
    }
} 