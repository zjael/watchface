package com.zjael.juddy.data.watchface

import android.content.Context
import android.graphics.drawable.Icon
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.ListUserStyleSetting
import com.zjael.juddy.R

/**
 * Represents watch face color style options the user can select (includes the unique id, the
 * complication style resource id, and general watch face color style resource ids).
 *
 * The companion object offers helper functions to translate a unique string id to the correct enum
 * and convert all the resource ids to their correct resources (with the Context passed in). The
 * renderer will use these resources to render the actual colors and ComplicationDrawables of the
 * watch face.
 */

@Keep
enum class ColorStyle(
    val id: String,
    @StringRes val nameResourceId: Int,
    @DrawableRes val iconResourceId: Int,
    @ColorRes val primaryColorId: Int,
    @ColorRes val secondaryColorId: Int,
    @ColorRes val tertiaryColorId: Int,
) {
    TEST(
        id = "test",
        nameResourceId = R.string.test_style_name,
        iconResourceId = R.drawable.test_style_icon,
        primaryColorId = R.color.test_primary_color,
        secondaryColorId = R.color.test_secondary_color,
        tertiaryColorId = R.color.test_tertiary_color,
    );

    companion object {
        fun getColorStyleConfig(id: String): ColorStyle {
            return when (id) {
                TEST.id -> TEST
                else -> TEST
            }
        }

        fun toOptionList(context: Context): List<ListUserStyleSetting.ListOption> {
            val colorStyleList = enumValues<ColorStyle>()

            return colorStyleList.map { style ->
                colorStyleToListOption(context, style)
            }
        }

        fun colorStyleToListOption(
            context: Context,
            style: ColorStyle
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
