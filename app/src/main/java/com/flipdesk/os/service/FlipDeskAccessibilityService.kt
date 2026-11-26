package com.flipdesk.os.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Optional accessibility service used solely to assist cover-screen state transitions
 * on devices where ColorOS restricts background activity launch on fold.
 * Never reads screen text or sensitive UI content.
 */
class FlipDeskAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
}
