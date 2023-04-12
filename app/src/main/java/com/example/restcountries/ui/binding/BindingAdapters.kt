package com.example.restcountries.ui.binding

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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

@BindingAdapter("commaSeparatedNumber")
fun separateNumber(view: TextView, number: Int) {
    view.text = "%,d".format(number)
}

@BindingAdapter("isVisible")
fun setViewVisibility(view: View, isVisible: Boolean) {
    if (isVisible) view.visibility = View.VISIBLE
    else view.visibility = View.GONE
}
