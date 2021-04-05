package com.yans.camerafeature

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class CameraActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1112
    }

    private lateinit var textureView: TextureView

    private lateinit var btnTakePhoto: Button

    private var cameraDevice: CameraDevice? = null

    private var previewWidth: Int = 0

    private var previewHeight: Int = 0

    private var cameraCaptureSessions: CameraCaptureSession? = null

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        textureView = findViewById(R.id.texture_view)
        btnTakePhoto = findViewById(R.id.btn_take_picture_2)

        textureView.surfaceTextureListener = textureListener
    }

    private fun openCamera() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val backCameras = manager.cameraIdList.filter {
                manager.getCameraCharacteristics(it)
                    .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
            if (backCameras.isNullOrEmpty()) {
                throw NullPointerException("Back facing camera not found")
            }
            val cameraId = backCameras.first()
            if (checkSelfPermission(
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CAMERA_PERMISSION
                )
                return
            }
            manager.openCamera(cameraId, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
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
                        cameraCaptureSessions = cameraCaptureSession
                        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                        try {
                            cameraCaptureSessions?.setRepeatingRequest(
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
                        mainExecutor,
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(
                    this,
                    "No permission granted",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        super.onPause()
        textureView.surfaceTextureListener = null
        cameraDevice?.close()
    }
}