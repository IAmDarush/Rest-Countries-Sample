package com.example.restcountries.ui.binding

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter(value = ["imageUrl", "errorDrawable", "placeholderDrawable"], requireAll = false)
fun loadImage(
    view: ImageView,
    url: String,
    error: Drawable? = null,
    placeholder: Drawable? = null
) {
    Glide.with(view.context)
        .load(url)
        .error(error)
        .placeholder(placeholder)
        .into(view)
}
