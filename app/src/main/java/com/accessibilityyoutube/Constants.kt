package com.accessibilityyoutube

enum class PlayerState {
    IDLE,
    SLEEPING_BEFORE,
    PLAYING,
    PAUSED,
    SLEEPING_AFTER,
    NOT_RUNNING
}

object ViewIds {
    const val player = "com.google.android.youtube:id/watch_player"
    const val playPause = "com.google.android.youtube:id/player_control_play_pause_replay_button"
}

object AppPackageNames {
    const val youtube = "com.google.android.youtube"
}