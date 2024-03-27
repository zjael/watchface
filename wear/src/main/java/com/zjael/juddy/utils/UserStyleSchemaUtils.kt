package com.zjael.juddy.utils

import android.content.Context
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import com.zjael.juddy.R
import com.zjael.juddy.data.watchface.AmbientStyle
import com.zjael.juddy.data.watchface.AmbientStyle.Companion.ambientStyleToListOption
import com.zjael.juddy.data.watchface.ColorStyle
import com.zjael.juddy.data.watchface.ColorStyle.Companion.colorStyleToListOption

// Keys to matched content in the user style settings. We listen for changes to these
// values in the renderer and if new, we will update the database and update the watch face
// being rendered.
const val COLOR_STYLE_SETTING = "color_style_setting"
const val AMBIENT_STYLE_SETTING = "ambient_style_setting"

/*
 * Creates user styles in the settings activity associated with the watch face, so users can
 * edit different parts of the watch face. In the renderer (after something has changed), the
 * watch face listens for a flow from the watch face API data layer and updates the watch face.
 */
fun createUserStyleSchema(context: Context): UserStyleSchema {
    // 1. Allows user to change the color styles of the watch face (if any are available).

    val colorStyleSetting =
        UserStyleSetting.ListUserStyleSetting(
            UserStyleSetting.Id(COLOR_STYLE_SETTING),
            context.resources,
            R.string.color_style_setting,
            R.string.color_style_setting_description,
            null,
            ColorStyle.toOptionList(context),
            listOf(WatchFaceLayer.BASE),
            defaultOption = colorStyleToListOption(context, ColorStyle.TEST)
        )

    val ambientStyleSetting =
        UserStyleSetting.ListUserStyleSetting(
            UserStyleSetting.Id(AMBIENT_STYLE_SETTING),
            context.resources,
            R.string.ambient_style_setting,
            R.string.ambient_style_setting_description,
            null,
            AmbientStyle.toOptionList(context),
            listOf(WatchFaceLayer.BASE),
            defaultOption = ambientStyleToListOption(context, AmbientStyle.OUTLINE)
        )

    return UserStyleSchema(
        listOf(
            colorStyleSetting,
            ambientStyleSetting
        )
    )
}
