package com.zjael.juddy.data.watchface

data class WatchFaceData(
    val colorStyle: ColorStyle = ColorStyle.TEST,
    val ambientStyle: AmbientStyle = AmbientStyle.OUTLINE,
    val militaryTime: Boolean = true,
    val bigAmbient: Boolean = false,
)