package com.zjael.juddy.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

val TopHalfRectShape: Shape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r1 = size.toRect()
        val r2 = Rect(r1.topLeft, Offset(r1.bottomRight.x, r1.bottomRight.y / 2f))
        return Outline.Rectangle(r2)
    }

    override fun toString(): String = "dev.rdnt.m8face.editor.getTopHalfRectShape"
}

val BottomHalfRectShape: Shape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r1 = size.toRect()
        val r2 = Rect(Offset(r1.topLeft.x, r1.bottomRight.y / 2f), r1.bottomRight)
        return Outline.Rectangle(r2)
    }

    override fun toString(): String = "com.zjael.juddy.editor.getBottomHalfRectShape"
}