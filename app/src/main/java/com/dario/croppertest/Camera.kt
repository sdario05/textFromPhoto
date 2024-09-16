package com.dario.croppertest

import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.selector.LensPositionSelector
import io.fotoapparat.selector.autoFocus
import io.fotoapparat.selector.back
import io.fotoapparat.selector.continuousFocusPicture
import io.fotoapparat.selector.firstAvailable
import io.fotoapparat.selector.fixed
import io.fotoapparat.selector.front
import io.fotoapparat.selector.highestFps
import io.fotoapparat.selector.highestResolution
import io.fotoapparat.selector.manualSensorSensitivity
import io.fotoapparat.selector.off
import io.fotoapparat.selector.standardRatio
import io.fotoapparat.selector.wideRatio
import java.io.File
import java.io.Serializable

sealed class Camera(val lensPosition: LensPositionSelector, val configuration: CameraConfiguration) {

    object Back : Camera(
        lensPosition = back(),
        configuration = CameraConfiguration(
            previewResolution = firstAvailable(
                wideRatio(highestResolution()),
                standardRatio(highestResolution())
            ),
            previewFpsRange = highestFps(),
            flashMode = off(),
            focusMode = firstAvailable(
                continuousFocusPicture(),
                autoFocus()
            ),
            frameProcessor = {
                // Do something with the preview frame
            },
            sensorSensitivity = manualSensorSensitivity(500)
        )
    )

    object Front : Camera(
        lensPosition = front(),
        configuration = CameraConfiguration(
            previewResolution = firstAvailable(
                wideRatio(highestResolution()),
                standardRatio(highestResolution())
            ),
            previewFpsRange = highestFps(),
            flashMode = off(),
            focusMode = firstAvailable(
                fixed(),
                autoFocus()
            ),
            sensorSensitivity = manualSensorSensitivity(500)
        )
    )
}