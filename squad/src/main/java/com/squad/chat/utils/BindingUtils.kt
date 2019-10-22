package com.squad.chat.utils

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import com.squareup.picasso.Picasso


@BindingAdapter("android:src")
fun ImageView.setImage(url: String?) {
    if (url == null) return
    Picasso.get()
            .load(url)
            .into(this)
}

@BindingConversion
fun convertBooleanToVisibility(expression: Boolean): Int {
    return if (expression) View.VISIBLE else View.GONE
}