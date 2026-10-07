package com.frostfel.animelist.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Asks for POST_NOTIFICATIONS (Android 13+). Must be created while the fragment or activity
 * is being initialised (as a property). The system stops showing the dialog after two denials.
 */
class NotificationPermissionRequest private constructor(
    caller: ActivityResultCaller,
    private val context: () -> Context?
) {
    constructor(fragment: Fragment) : this(fragment, { fragment.context })
    constructor(activity: ComponentActivity) : this(activity, { activity })

    private val launcher =
        caller.registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    fun requestIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val context = context() ?: return
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
