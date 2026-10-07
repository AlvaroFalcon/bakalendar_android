package com.frostfel.animelist.views.utils

import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

/** Serves the image from disk cache when possible so covers also show offline. */
fun ImageView.loadCached(url: String?) {
    val picasso = Picasso.get()
    picasso.load(url).networkPolicy(NetworkPolicy.OFFLINE).into(this, object : Callback {
        override fun onSuccess() = Unit
        override fun onError(e: Exception?) {
            picasso.load(url).into(this@loadCached)
        }
    })
}
