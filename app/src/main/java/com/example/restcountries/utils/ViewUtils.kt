package com.example.restcountries.utils

import android.content.res.Resources
import android.util.TypedValue


object ViewUtils {

    fun dpToPx(resources: Resources, dip: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dip, resources.displayMetrics
    )

}