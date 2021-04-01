package com.yans.camerafeature.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager

class CameraService(
    private val manager: CameraManager,
    private val cameraId: String
) {
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null

    fun openCamera(){
    }
}