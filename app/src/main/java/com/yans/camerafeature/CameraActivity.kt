package com.yans.camerafeature

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yans.camerafeature.Constants.REQUEST_CAMERA_PERMISSION


class CameraActivity : AppCompatActivity() {

    private lateinit var textureView: TextureView

    private lateinit var btnTakePhoto: Button

    private lateinit var cameraSession: CameraSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        textureView = findViewById(R.id.texture_view)
        btnTakePhoto = findViewById(R.id.btn_take_picture_2)

        cameraSession = CameraSession(this, textureView)
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
        cameraSession.resumeSession()
    }

    override fun onPause() {
        super.onPause()
        cameraSession.pauseSession()
    }
}