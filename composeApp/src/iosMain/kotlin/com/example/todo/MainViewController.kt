package com.example.todo

import androidx.compose.ui.window.ComposeUIViewController
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

fun MainViewController() = ComposeUIViewController {
    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
        UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
        completionHandler = { _, _ -> }
    )
    TodoApp()
}
