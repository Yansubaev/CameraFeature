package com.yans.camerafeature

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.yans.camerafeature.Constants.REQUEST_CAMERA_PERMISSION

class CameraSession(
    private val activity: Activity,
    private val textureView: TextureView
) {
    private val cameraManager: CameraManager =
        activity.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager

    private var cameraDevice: CameraDevice? = null

    private var previewWidth: Int = 0

    private var previewHeight: Int = 0

    private var textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            previewWidth = width
            previewHeight = height
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            previewWidth = width
            previewHeight = height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    fun resumeSession(){
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = textureListener
        }
    }

    fun pauseSession(){
        textureView.surfaceTextureListener = null
        cameraDevice?.close()
    }

    private fun createCameraPreview() {
        try {
            val texture = textureView.surfaceTexture
            texture?.setDefaultBufferSize(previewWidth, previewHeight)
            val surface = Surface(texture)
            val captureRequestBuilder =
                cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(surface)
            val stateCallback = object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    captureRequestBuilder?.let { builder ->
                        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                        try {
                            cameraCaptureSession.setRepeatingRequest(
                                builder.build(),
                                null,
                                null
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {}
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                cameraDevice?.createCaptureSession(
                    SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        listOf(OutputConfiguration(surface)),
                        activity.mainExecutor,
                        stateCallback
                    )
                )
            } else {
                cameraDevice?.createCaptureSession(
                    listOf(surface),
                    stateCallback,
                    null
                )
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        try {
            val backCameras = cameraManager.cameraIdList.filter {
                cameraManager.getCameraCharacteristics(it)
                    .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
            if (backCameras.isNullOrEmpty()) {
                throw NullPointerException("Back facing camera not found")
            }
            val cameraId = backCameras.first()
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CAMERA_PERMISSION
                )
                return
            }
            cameraManager.openCamera(cameraId, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }
}