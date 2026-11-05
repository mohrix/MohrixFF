package com.mohrix.ff

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.core.app.NotificationCompat

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var menuView: View? = null
    private lateinit var params: WindowManager.LayoutParams

    override fun onCreate() {
        super.onCreate()
        
        val notification = NotificationCompat.Builder(this, "mohrix_channel")
            .setContentTitle("Mohrix FF")
            .setContentText("Service is running")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .build()
        startForeground(1, notification)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        val iconView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null)
        (iconView as android.widget.TextView).text = "M"
        iconView.setBackgroundColor(0xFF000000.toInt())
        iconView.setTextColor(0xFFFF0000.toInt())
        iconView.setPadding(30, 30, 30, 30)

        floatingView = iconView
        windowManager.addView(floatingView, params)

        floatingView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (event.rawX - initialTouchX < 10) toggleMenu()
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun toggleMenu() {
        if (menuView != null) {
            windowManager.removeView(menuView)
            menuView = null
            return
        }

        val menu = LayoutInflater.from(this).inflate(R.layout.menu_floating, null)
        val menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        menu.findViewById<Switch>(R.id.swAimbot).setOnCheckedChangeListener { _, isChecked -> NativeLib.toggleFeature("aimbot", isChecked) }
        menu.findViewById<Switch>(R.id.swSpin).setOnCheckedChangeListener { _, isChecked -> NativeLib.toggleFeature("spin", isChecked) }
        menu.findViewById<Switch>(R.id.swFly).setOnCheckedChangeListener { _, isChecked -> NativeLib.toggleFeature("fly", isChecked) }

        menuView = menu
        windowManager.addView(menuView, menuParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
        if (menuView != null) windowManager.removeView(menuView)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
