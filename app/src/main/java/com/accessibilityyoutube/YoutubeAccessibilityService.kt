package com.accessibilityyoutube

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.accessibilityyoutube.SharedState.playTime
import com.accessibilityyoutube.SharedState.playerState
import com.accessibilityyoutube.SharedState.sleepAfterPlayTime
import com.accessibilityyoutube.SharedState.sleepBeforePlayTime
import java.time.LocalDateTime

class YoutubeAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var waitingToPlay = false
    private var buttonsHidden = false
    private var startPlayingDate: LocalDateTime? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d("YoutubeAccessibility", "onAccessibilityEvent")
        val rootNode = rootInActiveWindow
        if (event.packageName == AppPackageNames.youtube) {
            rootNode?.let { node ->
                if (playerState == PlayerState.IDLE) {
                    showButtons(node)
                    performPauseYouTubeVideo(node, PlayerState.SLEEPING_BEFORE)
                } else if (playerState == PlayerState.SLEEPING_BEFORE) {
                    if (!waitingToPlay) {
                        handler.postDelayed({
                            waitingToPlay = true
                            showButtons(node)
                            performPlayYouTubeVideo(node)
                        }, sleepBeforePlayTime.value.toLong() * 1000)
                    }
                } else if (playerState == PlayerState.PLAYING) {
                    showButtons(node)
                    verifyTimeToPauseVideo(node)
                }
            }
        }
    }

    private fun showButtons(node: AccessibilityNodeInfo) {
        if (buttonsHidden) {
            pressVideoView(node)
            buttonsHidden = false
        }
    }

    override fun onInterrupt() {
        Log.d("YoutubeAccessibility", "Service interrupted")
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            packageNames = arrayOf(AppPackageNames.youtube)
        }
        serviceInfo = info
        Log.d("YoutubeAccessibility", "Service connected")
    }

    private fun performPauseYouTubeVideo(node: AccessibilityNodeInfo, nextState: PlayerState) {
        Log.d("YoutubeAccessibility", "performPauseYouTubeVideo")
        val pauseNode = getValidNode(node, ViewIds.playPause)
        pauseNode?.let {
            val description = it.contentDescription.toString().lowercase().trim()
            if (description.contains("pause")) {
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                playerState = nextState
                Log.d("YoutubeAccessibility", "pause performed")
            }
        } ?: kotlin.run {
            buttonsHidden = true
        }
    }

    private fun performPlayYouTubeVideo(node: AccessibilityNodeInfo) {
        Log.d("YoutubeAccessibility", "performPlayYouTubeVideo")
        val playNode = getValidNode(node, ViewIds.playPause)
        playNode?.let {
            val description = it.contentDescription.toString().lowercase().trim()
            if (description.contains("play")) {
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("YoutubeAccessibility", "play performed")
            }
            playerState = PlayerState.PLAYING
            startPlayingDate = LocalDateTime.now()
            waitingToPlay = false
        } ?: kotlin.run {
            buttonsHidden = true
        }
    }

    private fun getValidNode(node: AccessibilityNodeInfo, id: String): AccessibilityNodeInfo? {
        Log.d("YoutubeAccessibility", "getValidNode")
        val nodeList = node.findAccessibilityNodeInfosByViewId(id)
        if (nodeList.isNotEmpty()) {
            return nodeList.first()
        }
        return null
    }

    private fun verifyTimeToPauseVideo(rootNode: AccessibilityNodeInfo) {
        Log.d("YoutubeAccessibility", "verifyTimeToPauseVideo")
        if (playerState != PlayerState.PAUSED) {
            val timeElapsedNode = findNodeByContentDescriptionContaining(rootNode, "elapsed")
            if (timeElapsedNode != null) {
                val contentDescription = timeElapsedNode.contentDescription.toString()
                Log.d("YoutubeAccessibility", "contentDescription: $contentDescription")
                val timeElapsedText = contentDescription.split("elapsed").first()
                val secondsElapsed = parseTextToSeconds(timeElapsedText.trim())
                if (secondsElapsed > 0) {
                    Log.d("YoutubeAccessibility", "time elapsed text: $timeElapsedText")
                    Log.d("YoutubeAccessibility", "time elapsed: $secondsElapsed")
                }

                if (secondsElapsed >= playTime.value.toInt() && isAtLeastTwoSecondsAfter()) {
                    performPauseYouTubeVideo(rootNode, PlayerState.SLEEPING_AFTER)

                    handler.postDelayed({
                        launchPoCApp()
                    }, sleepAfterPlayTime.value.toLong() * 1000)
                }
            } else {
                buttonsHidden = true
            }
        }
    }

    private fun isAtLeastTwoSecondsAfter(): Boolean {
        val now = LocalDateTime.now()
        return now.isAfter(startPlayingDate?.plusSeconds(2))

    }

    private fun pressVideoView(rootNode: AccessibilityNodeInfo) {
        Log.d("YoutubeAccessibility", "pressVideoView")
        val videoView = getValidNode(rootNode, ViewIds.player)
        videoView?.let { node ->
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("YoutubeAccessibility", "screen pressed")
        }
    }
    private fun parseTextToSeconds(text: String): Int {
        val regexSeconds = """(\d+) seconds""".toRegex()
        val matchResultSeconds = regexSeconds.matchEntire(text)
        if (matchResultSeconds != null) {
            val (seconds) = matchResultSeconds.destructured
            return seconds.toInt()
        }

        val regexMinute = """(\d+) minutes (\d+) seconds""".toRegex()
        val matchResultMinute = regexMinute.matchEntire(text)
        if (matchResultMinute != null) {
            val (minute, seconds) = matchResultMinute.destructured
            return (minute.toInt() * 60) + seconds.toInt()
        }

        val regexMinutes = """(\d+) minutes (\d+) seconds""".toRegex()
        val matchResultMinutes = regexMinutes.matchEntire(text)
        if (matchResultMinutes != null) {
            val (minutes, seconds) = matchResultMinutes.destructured
            return (minutes.toInt() * 60) + seconds.toInt()
        }

        val regexHours = """(\d+) hours (\d+) minutes (\d+) seconds""".toRegex()
        val matchResultHours = regexHours.matchEntire(text)
        if (matchResultHours != null) {
            val (hours, minutes, seconds) = matchResultHours.destructured
            return (hours.toInt() * 60) + (minutes.toInt() * 60) + seconds.toInt()
        }

        return -1
    }

    private fun findNodeByContentDescriptionContaining(
        rootNode: AccessibilityNodeInfo,
        contentDescription: String
    ): AccessibilityNodeInfo? {
        val nodeText = rootNode.contentDescription
        if (nodeText != null) {
            val nodeTextStr = when (nodeText) {
                is String -> nodeText
                is SpannableString -> nodeText.toString()
                else -> null
            }
            if (nodeTextStr != null && nodeTextStr.contains(contentDescription)) {
                return rootNode
            }
        }

        for (i in 0 until rootNode.childCount) {
            val childNode = rootNode.getChild(i)
            if (childNode != null) {
                val result = findNodeByContentDescriptionContaining(childNode, contentDescription)
                if (result != null) {
                    return result
                }
            }
        }

        return null
    }

    private fun launchPoCApp() {
        Log.d("YoutubeAccessibility", "launchPoCApp")
        playerState = PlayerState.NOT_RUNNING
        val launchIntent = packageManager.getLaunchIntentForPackage("com.accessibilityyoutube")
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
    }
} 