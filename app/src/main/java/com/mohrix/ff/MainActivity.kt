package com.mohrix.ff

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private val SHIZUKU_PERMISSION_CODE = 100
    private val STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btnStart)

        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            } else if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            } else {
                checkShizuku()
            }
        }
    }

    private fun checkShizuku() {
        if (Shizuku.isPreV11() || Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            doInjection()
        } else {
            Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
        }
    }

    private fun doInjection() {
        Toast.makeText(this, "Injecting Payload...", Toast.LENGTH_SHORT).show()
        try {
            val cmd = "cp /sdcard/Download/payload.zip /sdcard/Android/data/com.dts.freefireth/files/"
            val process = Shizuku.newProcess(arrayOf("sh", "-c", cmd), null, null)
            process.waitFor()
            Toast.makeText(this, "Injection Successful", Toast.LENGTH_SHORT).show()
            
            startService(Intent(this, FloatingService::class.java))
            moveTaskToBack(true)
        } catch (e: Exception) {
            Toast.makeText(this, "Injection Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SHIZUKU_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doInjection()
        }
    }
}
