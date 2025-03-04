package com.vpk.eduseed

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein)

        val splashImage: ImageView = findViewById(R.id.iv_splash)
        splashImage.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this,RegisterActivity::class.java))
            finish()
        }, 1500)
    }
}
