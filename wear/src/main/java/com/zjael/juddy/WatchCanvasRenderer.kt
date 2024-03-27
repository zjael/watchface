package com.zjael.juddy

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import kotlinx.coroutines.*
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.provider.CalendarContract
import android.util.FloatProperty
import android.util.Log
import android.view.SurfaceHolder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.toUpperCase
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.withScale
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import kotlin.math.pow
import kotlin.math.sqrt
import com.zjael.juddy.data.watchface.WatchFaceColorPalette.Companion.convertToWatchFaceColorPalette
import com.zjael.juddy.data.watchface.WatchFaceData

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Default for how long each frame is displayed at expected frame rate.
private const val FRAME_PERIOD_MS_DEFAULT: Long = 16L

class WatchCanvasRenderer (
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    private val complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    canvasType: Int
) : Renderer.CanvasRenderer2<WatchCanvasRenderer.CanvasSharedAssets>(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    canvasType,
    FRAME_PERIOD_MS_DEFAULT,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false
) {
    class CanvasSharedAssets : SharedAssets {
        override fun onDestroy() {
        }
    }

    override suspend fun createSharedAssets(): CanvasSharedAssets {
        return CanvasSharedAssets()
    }

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    // Default size of watch face drawing area, that is, a no size rectangle. Will be replaced with valid dimensions from the system.
    private var currentWatchFaceSize = Rect(0, 0, 0, 0)
    private val ambientTransitionMs = 1000L
    private var drawProperties = DrawProperties()
    private var watchFaceData: WatchFaceData = WatchFaceData()

    // Converts resource ids into Colors and ComplicationDrawable.
    private var watchFaceColors = convertToWatchFaceColorPalette(
        context,
        watchFaceData.colorStyle,
    )

    private val primaryPaint = Paint().apply {
        isAntiAlias = true
        textSize = 32f
        typeface = context.resources.getFont(R.font.urbanist)
        color = watchFaceColors.primaryColor
    }

    private val secondaryPaint = Paint().apply {
        isAntiAlias = true
        textSize = 22f
        typeface = context.resources.getFont(R.font.urbanist)
        color = watchFaceColors.secondaryColor
    }

    private val accentPaint = Paint().apply {
        isAntiAlias = true
        textSize = 32f
        typeface = context.resources.getFont(R.font.urbanist)
        color = watchFaceColors.tertiaryColor
    }

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
    }

    private val datePaint = Paint().apply {
        isAntiAlias = true
        typeface = context.resources.getFont(R.font.urbanist)
        textSize = 24F
        textAlign = Paint.Align.CENTER
        color = watchFaceColors.tertiaryColor
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: CanvasSharedAssets
    ) {
        canvas.drawColor(Color.BLACK)

        if(renderParameters.drawMode == DrawMode.AMBIENT) {
            primaryPaint.typeface = context.resources.getFont(R.font.urbanist)
        } else {
            primaryPaint.typeface = Typeface.create(context.resources.getFont(R.font.urbanist), Typeface.BOLD)
        }

        //WatchFace(watchFaceData, complicationSlotsManager)

        drawTime(canvas, bounds, zonedDateTime);
        drawDate(canvas, bounds, zonedDateTime);
        //drawCalendar(canvas, bounds, zonedDateTime, accentPaint);
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: CanvasSharedAssets
    ) {
        TODO("Not yet implemented")
    }

    private fun drawDate(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
    ) {
        val day: Int = zonedDateTime.dayOfMonth
        val month: Int = zonedDateTime.monthValue
        val dayOfWeek: String = zonedDateTime.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))

        val formattedDate: String = String.format("%02d.%02d", day, month)
        val canvasText = "${dayOfWeek.uppercase()}  $formattedDate"

        val p = Paint(primaryPaint)
        val textWidth: Float = p.measureText(canvasText)
        val textHeight: Float = p.fontMetrics.descent - p.fontMetrics.ascent
        val posX = bounds.exactCenterX() - textWidth / 2.0f
        val posY = bounds.top.toFloat() + textHeight + 28f

        canvas.drawText(canvasText, posX, posY, p)
    }

    private fun drawTime(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
    ) {
        val hour: Int = zonedDateTime.hour
        val minute: Int = zonedDateTime.minute
        val formattedTime: String = String.format("%02d:%02d", hour, minute)

        val p = Paint(primaryPaint)
        p.textSize = 96f;

        val posX = bounds.exactCenterX() - p.measureText(formattedTime) / 2.0f
        val posY = bounds.exactCenterY() - (p.fontMetrics.descent - p.fontMetrics.ascent) / 2.0f + (p.fontMetrics.descent - p.fontMetrics.ascent) / 2.0f

        canvas.drawText(formattedTime, posX, posY, p)
    }

    @SuppressLint("Range")
    private fun drawCalendar(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        paint: Paint
    ) {
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )

        val selection = "${CalendarContract.Events.DTSTART} >= ?"
        val selectionArgs = arrayOf(System.currentTimeMillis().toString())

        val contentResolver: ContentResolver = context.contentResolver
        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val eventId = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID))
                val title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE))
                val description = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION))
                val startTime = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART))
                val endTime = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND))

                Log.d("CalendarData", "Event ID: $eventId, Title: $title, Description: $description, StartTime: $startTime, EndTime: $endTime")
            }
            cursor.close()
        }
    }

    private class DrawProperties(
        var timeScale: Float = 0f
    ) {
        companion object {
            val TIME_SCALE =
                object : FloatProperty<DrawProperties>("timeScale") {
                    override fun setValue(obj: DrawProperties, value: Float) {
                        obj.timeScale = value
                    }

                    override fun get(obj: DrawProperties): Float {
                        return obj.timeScale
                    }
                }
        }
    }

    private fun easeInOutQuint(x: Float): Float {
        return if (x < 0.5f) {
            16 * x * x * x * x * x
        } else {
            1 - (-2f * x + 2f).pow(5f) / 2
        }
    }

    private fun easeInOutCirc(x: Float): Float {
        return if (x < 0.5f) {
            (1f - sqrt(1f - (2f * x).pow(2f))) / 2f
        } else {
            (sqrt(1f - (-2f * x + 2f).pow(2f)) + 1f) / 2f
        }
    }

    companion object {
        private const val TAG = "WatchCanvasRenderer"
    }
}