package com.zjael.juddy.data.watchface

import android.content.Context
import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.ListUserStyleSetting
import com.zjael.juddy.R

enum class AmbientStyle(
    val id: String,
    @StringRes val nameResourceId: Int,
    @DrawableRes val iconResourceId: Int,
) {
    OUTLINE(
        id = "outline",
        nameResourceId = R.string.outline_ambient_style_name,
        iconResourceId = R.drawable.outline_style_icon,
    ),
    BOLD_OUTLINE(
        id = "bold_outline",
        nameResourceId = R.string.bold_outline_ambient_style_name,
        iconResourceId = R.drawable.bold_outline_style_icon,
    ),
    FILLED(
        id = "filled",
        nameResourceId = R.string.filled_ambient_style_name,
        iconResourceId = R.drawable.filled_style_icon,
    );

    companion object {
        fun getAmbientStyleConfig(id: String): AmbientStyle {
            return when (id) {
                OUTLINE.id -> OUTLINE
                BOLD_OUTLINE.id -> BOLD_OUTLINE
                FILLED.id -> FILLED
                else -> OUTLINE
            }
        }

        fun toOptionList(context: Context): List<ListUserStyleSetting.ListOption> {
            val colorStyleIdAndResourceIdsList = enumValues<AmbientStyle>()

            return colorStyleIdAndResourceIdsList.map { style ->
                ambientStyleToListOption(context, style)
            }
        }

        fun ambientStyleToListOption(
            context: Context,
            style: AmbientStyle
        ): ListUserStyleSetting.ListOption {
            return ListUserStyleSetting.ListOption(
                UserStyleSetting.Option.Id(style.id),
                context.resources,
                style.nameResourceId,
                Icon.createWithResource(
                    context,
                    style.iconResourceId
                )
            )
        }
    }
}