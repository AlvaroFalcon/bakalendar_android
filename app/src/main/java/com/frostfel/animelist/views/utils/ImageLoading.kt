package com.frostfel.animelist.views.utils

import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

/** Serves the image from disk cache when possible so covers also show offline. */
fun ImageView.loadCached(url: String?, onLoaded: (() -> Unit)? = null) {
    val picasso = Picasso.get()
    val loaded = object : Callback {
        override fun onSuccess() {
            onLoaded?.invoke()
        }

        override fun onError(e: Exception?) = Unit
    }
    picasso.load(url).networkPolicy(NetworkPolicy.OFFLINE).into(this, object : Callback {
        override fun onSuccess() = loaded.onSuccess()
        override fun onError(e: Exception?) {
            picasso.load(url).into(this@loadCached, loaded)
        }
    })
}
