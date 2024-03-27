package com.zjael.juddy.editor

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.editor.EditorSession
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import com.zjael.juddy.utils.*
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class WatchFaceConfigStateHolder(
    private val scope: CoroutineScope,
    private val activity: ComponentActivity
) {
    private var launchInProgress: Boolean = false

    private lateinit var editorSession: EditorSession

    // Keys from Watch Face Data Structure
    private lateinit var colorStyleKey: UserStyleSetting.ListUserStyleSetting
    private lateinit var ambientStyleKey: UserStyleSetting.ListUserStyleSetting
    private lateinit var secondsStyleKey: UserStyleSetting.ListUserStyleSetting
    private lateinit var militaryTimeKey: UserStyleSetting.BooleanUserStyleSetting
    private lateinit var bigAmbientKey: UserStyleSetting.BooleanUserStyleSetting

    val uiState: StateFlow<EditWatchFaceUiState> =
        flow<EditWatchFaceUiState> {
            editorSession = EditorSession.createOnWatchEditorSession(
                activity = activity
            )

            extractsUserStyles(editorSession.userStyleSchema)

            emitAll(
                combine(
                    editorSession.userStyle,
                    editorSession.complicationsPreviewData,
                ) { userStyle, complicationsPreviewData ->
                    yield()
                    EditWatchFaceUiState.Success(
                        createWatchFacePreview(
                            userStyle,
                            complicationsPreviewData,
                        )
                    )
                }
            )
        }
            .stateIn(
                scope + Dispatchers.Main.immediate,
                SharingStarted.Eagerly,
                EditWatchFaceUiState.Loading("Initializing")
            )

    private fun extractsUserStyles(userStyleSchema: UserStyleSchema) {
        // Loops through user styles and retrieves user editable styles.
        for (setting in userStyleSchema.userStyleSettings) {
            when (setting.id.toString()) {
                COLOR_STYLE_SETTING -> {
                    colorStyleKey = setting as UserStyleSetting.ListUserStyleSetting
                }
                AMBIENT_STYLE_SETTING -> {
                    ambientStyleKey = setting as UserStyleSetting.ListUserStyleSetting
                }
            }
        }
    }

    /* Creates a new bitmap render of the updated watch face and passes it along (with all the other
     * updated values) to the Activity to render.
     */
    private fun createWatchFacePreview(
        userStyle: UserStyle,
        complicationsPreviewData: Map<Int, ComplicationData>,
    ): UserStylesAndPreview {

        Log.d(TAG, "createWatchFacePreview()")

        val instant = LocalDateTime.parse("2020-10-10T22:09:36")
            .atZone(ZoneId.of("UTC"))
            .toInstant()

        val bitmap = editorSession.renderWatchFaceToBitmap(
            RenderParameters(
                DrawMode.INTERACTIVE,
                WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
            ),
            instant,
            complicationsPreviewData
        )

        val colorStyle =
            userStyle[colorStyleKey] as UserStyleSetting.ListUserStyleSetting.ListOption

        val ambientStyle =
            userStyle[ambientStyleKey] as UserStyleSetting.ListUserStyleSetting.ListOption

        val secondsStyle =
            userStyle[secondsStyleKey] as UserStyleSetting.ListUserStyleSetting.ListOption

        val militaryTime =
            userStyle[militaryTimeKey] as UserStyleSetting.BooleanUserStyleSetting.BooleanOption

        val bigAmbient =
            userStyle[bigAmbientKey] as UserStyleSetting.BooleanUserStyleSetting.BooleanOption

        return UserStylesAndPreview(
            colorStyleId = colorStyle.id.toString(),
            ambientStyleId = ambientStyle.id.toString(),
            secondsStyleId = secondsStyle.id.toString(),
            militaryTime = militaryTime.value,
            bigAmbient = bigAmbient.value,
            previewImage = bitmap,
        )
    }

    fun setComplication(complicationLocation: Int) {
        if (launchInProgress) {
            return
        }
        launchInProgress = true

        val complicationSlotId = when (complicationLocation) {
            LEFT_COMPLICATION_ID -> {
                LEFT_COMPLICATION_ID
            }

            RIGHT_COMPLICATION_ID -> {
                RIGHT_COMPLICATION_ID
            }

            TOP_COMPLICATION_ID -> {
                TOP_COMPLICATION_ID
            }

            BOTTOM_COMPLICATION_ID -> {
                BOTTOM_COMPLICATION_ID
            }

            else -> {
                launchInProgress = false
                return
            }
        }

        scope.launch(Dispatchers.Main.immediate) {
            editorSession.openComplicationDataSourceChooser(complicationSlotId)
        }.invokeOnCompletion(
            fun(_: Throwable?) {
                launchInProgress = false
            }
        )
    }

    fun setColorStyle(colorStyleId: String) {
        val userStyleSettingList = editorSession.userStyleSchema.userStyleSettings

        // Loops over all UserStyleSettings (basically the keys in the map) to find the setting for
        // the color style (which contains all the possible options for that style setting).
        for (userStyleSetting in userStyleSettingList) {
            if (userStyleSetting.id == UserStyleSetting.Id(COLOR_STYLE_SETTING)) {
                val colorUserStyleSetting =
                    userStyleSetting as UserStyleSetting.ListUserStyleSetting

                // Loops over the UserStyleSetting.Option colors (all possible values for the key)
                // to find the matching option, and if it exists, sets it as the color style.
                for (colorOptions in colorUserStyleSetting.options) {
                    if (colorOptions.id.toString() == colorStyleId) {
                        setUserStyleOption(colorStyleKey, colorOptions)
                    }
                }
            }
        }
    }

    fun setAmbientStyle(ambientStyleId: String) {
        val userStyleSettingList = editorSession.userStyleSchema.userStyleSettings

        // Loops over all UserStyleSettings (basically the keys in the map) to find the setting for
        // the color style (which contains all the possible options for that style setting).
        for (ambientStyleSetting in userStyleSettingList) {
            if (ambientStyleSetting.id == UserStyleSetting.Id(AMBIENT_STYLE_SETTING)) {
                val ambientUserStyleSetting =
                    ambientStyleSetting as UserStyleSetting.ListUserStyleSetting

                // Loops over the UserStyleSetting.Option colors (all possible values for the key)
                // to find the matching option, and if it exists, sets it as the color style.
                for (colorOptions in ambientUserStyleSetting.options) {
                    if (colorOptions.id.toString() == ambientStyleId) {
                        setUserStyleOption(ambientStyleKey, colorOptions)
                    }
                }
            }
        }
    }

    fun setMilitaryTime(enabled: Boolean) {
        setUserStyleOption(
            militaryTimeKey,
            UserStyleSetting.BooleanUserStyleSetting.BooleanOption.from(enabled)
        )
    }

    fun setBigAmbient(enabled: Boolean) {
        setUserStyleOption(
            bigAmbientKey,
            UserStyleSetting.BooleanUserStyleSetting.BooleanOption.from(enabled)
        )
    }

    // Saves User Style Option change back to the back to the EditorSession.
    // Note: The UI widgets in the Activity that can trigger this method (through the 'set' methods)
    // will only be enabled after the EditorSession has been initialized.
    private fun setUserStyleOption(
        userStyleSetting: UserStyleSetting,
        userStyleOption: UserStyleSetting.Option
    ) {
        // TODO: As of watchface 1.0.0-beta01 We can't use MutableStateFlow.compareAndSet, or
        //       anything that calls through to that (like MutableStateFlow.update) because
        //       MutableStateFlow.compareAndSet won't properly update the user style.
        val mutableUserStyle = editorSession.userStyle.value.toMutableUserStyle()
        mutableUserStyle[userStyleSetting] = userStyleOption
        editorSession.userStyle.value = mutableUserStyle.toUserStyle()
    }

    sealed class EditWatchFaceUiState {
        data class Success(val userStylesAndPreview: UserStylesAndPreview) : EditWatchFaceUiState()
        data class Loading(val message: String) : EditWatchFaceUiState()
        data class Error(val exception: Throwable) : EditWatchFaceUiState()
    }

    data class UserStylesAndPreview(
        val colorStyleId: String,
        val ambientStyleId: String,
        val secondsStyleId: String,
        val militaryTime: Boolean,
        val bigAmbient: Boolean,
        val previewImage: Bitmap
    )

    companion object {
        private const val TAG = "WatchFaceConfigStateHolder"
    }
}