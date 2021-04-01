package com.yans.camerafeature

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_FOR_CAMERA = 12

    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_FOR_CAMERA)
        }

        cameraManager.cameraIdList.forEach {
            Log.i(javaClass.simpleName, it)

            val chars = cameraManager.getCameraCharacteristics(it)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if(requestCode == REQUEST_CODE_FOR_CAMERA){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startCamera()
            }
        }
    }

    private fun startCamera(){
    }
}