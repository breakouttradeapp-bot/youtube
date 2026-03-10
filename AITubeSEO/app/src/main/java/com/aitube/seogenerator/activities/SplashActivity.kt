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
            try {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

                @Suppress("DEPRECATION")
                overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            } catch (_: Exception) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimations()

        // Navigate after 3 seconds
        splashHandler.postDelayed(navigateRunnable, 3000)
    }

    private fun startAnimations() {

        try {

            binding.lottieAnim?.apply {
                scaleX = 0.8f
                scaleY = 0.8f
                alpha = 0f

                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setStartDelay(200)
                    .setDuration(700)
                    .start()
            }

            binding.tvAppTitle?.apply {
                alpha = 0f
                animate()
                    .alpha(1f)
                    .setStartDelay(900)
                    .setDuration(600)
                    .start()
            }

            binding.tvTagline?.apply {
                alpha = 0f
                translationY = 40f

                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(1400)
                    .setDuration(600)
                    .start()
            }

            binding.tvVersion?.apply {
                alpha = 0f
                animate()
                    .alpha(1f)
                    .setStartDelay(1800)
                    .setDuration(500)
                    .start()
            }

        } catch (_: Exception) {
            // Prevent splash crash if animation fails
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Prevent memory leaks
        splashHandler.removeCallbacks(navigateRunnable)
    }
}
