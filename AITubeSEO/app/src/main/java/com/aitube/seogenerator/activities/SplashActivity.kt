package com.aitube.seogenerator.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.aitube.seogenerator.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val splashHandler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        if (!isFinishing && !isDestroyed) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimations()
        splashHandler.postDelayed(navigateRunnable, 3000)
    }

    private fun startAnimations() {
        try {
            // Logo zoom in
            binding.lottieAnim.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setStartDelay(200).setDuration(700).start()

            // Title fade in
            binding.tvAppTitle.alpha = 0f
            binding.tvAppTitle.animate()
                .alpha(1f).setStartDelay(900).setDuration(600).start()

            // Tagline slide up
            binding.tvTagline.alpha = 0f
            binding.tvTagline.translationY = 40f
            binding.tvTagline.animate()
                .alpha(1f).translationY(0f).setStartDelay(1400).setDuration(600).start()

            // Version fade in
            binding.tvVersion.alpha = 0f
            binding.tvVersion.animate()
                .alpha(1f).setStartDelay(1800).setDuration(500).start()
        } catch (e: Exception) {
            // Animation failure should never crash the app
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove pending callbacks to prevent memory leaks / post-destroy crashes
        splashHandler.removeCallbacks(navigateRunnable)
    }
}
