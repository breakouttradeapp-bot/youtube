package com.aitube.seogenerator.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.aitube.seogenerator.R
import com.aitube.seogenerator.databinding.ActivityMainBinding
import com.aitube.seogenerator.models.HistoryItem
import com.aitube.seogenerator.models.UiState
import com.aitube.seogenerator.utils.*
import com.aitube.seogenerator.viewmodel.MainViewModel
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var prefs: PrefsManager
    private val gson = Gson()

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isActivityVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PrefsManager(this)
        applyDarkMode()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        initAds()
        setupClickListeners()
        observeViewModel()
        updateLimitUI()
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
        updateLimitUI()
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
    }

    private fun applyDarkMode() {
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun initAds() {
        try {
            MobileAds.initialize(this) {
                loadInterstitialAd()
                loadRewardedAd()
            }
            binding.bannerAdView.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            // Ads should never crash the app
        }
    }

    private fun loadInterstitialAd() {
        try {
            InterstitialAd.load(
                this,
                Constants.ADMOB_INTERSTITIAL_ID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
        } catch (e: Exception) { /* swallow ad errors */ }
    }

    private fun loadRewardedAd() {
        try {
            RewardedAd.load(
                this,
                Constants.ADMOB_REWARDED_ID,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                    }
                }
            )
        } catch (e: Exception) { /* swallow ad errors */ }
    }

    private fun setupClickListeners() {
        binding.btnGenerateSeo.setOnClickListener {
            val topic = binding.etTopic.text?.toString()?.trim() ?: ""
            if (topic.isEmpty()) {
                binding.tilTopic.error = "Please enter a video topic"
                return@setOnClickListener
            }
            binding.tilTopic.error = null
            if (!checkLimit()) return@setOnClickListener
            if (!NetworkUtils.isAvailable(this)) {
                showError("No internet connection. Please check your network.")
                return@setOnClickListener
            }
            viewModel.generateSeo(topic)
        }

        binding.btnGenerateShorts.setOnClickListener {
            val topic = binding.etTopic.text?.toString()?.trim() ?: ""
            if (topic.isEmpty()) {
                binding.tilTopic.error = "Please enter a video topic"
                return@setOnClickListener
            }
            binding.tilTopic.error = null
            if (!checkLimit()) return@setOnClickListener
            if (!NetworkUtils.isAvailable(this)) {
                showError("No internet connection. Please check your network.")
                return@setOnClickListener
            }
            viewModel.generateShorts(topic)
        }

        binding.btnWatchAd.setOnClickListener { showRewardedAd() }
    }

    private fun checkLimit(): Boolean {
        if (prefs.isUnlocked()) return true
        if (prefs.getGenerationCount() >= Constants.FREE_GENERATION_LIMIT) {
            showLimitDialog()
            return false
        }
        return true
    }

    private fun showLimitDialog() {
        if (isFinishing || isDestroyed) return
        try {
            MaterialAlertDialogBuilder(this)
                .setTitle("⚡ Daily Limit Reached")
                .setMessage(
                    "You've used all ${Constants.FREE_GENERATION_LIMIT} free generations.\n\n" +
                    "Watch a short ad to unlock unlimited generations for 30 minutes!"
                )
                .setPositiveButton("Watch Ad 🎬") { _, _ -> showRewardedAd() }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) { /* window detached */ }
    }

    private fun showRewardedAd() {
        try {
            val ad = rewardedAd
            if (ad != null && !isFinishing && !isDestroyed) {
                ad.show(this) { _ ->
                    if (!isFinishing && !isDestroyed) {
                        val unlockUntil = System.currentTimeMillis() +
                                (Constants.REWARDED_UNLOCK_MINUTES * 60 * 1000)
                        prefs.setUnlockUntil(unlockUntil)
                        prefs.resetCount()
                        updateLimitUI()
                        Toast.makeText(this, "🎉 Unlocked for 30 minutes!", Toast.LENGTH_LONG).show()
                    }
                }
                rewardedAd = null
                loadRewardedAd()
            } else {
                Toast.makeText(this, "Ad not ready yet. Please try again.", Toast.LENGTH_SHORT).show()
                loadRewardedAd()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ad not available right now.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun maybeShowInterstitial() {
        try {
            val count = prefs.getGenerationCount()
            val ad = interstitialAd
            if (ad != null && count > 0 && count % Constants.INTERSTITIAL_EVERY_N == 0
                && !isFinishing && !isDestroyed && isActivityVisible) {
                ad.show(this)
                interstitialAd = null
                loadInterstitialAd()
            }
        } catch (e: Exception) { /* ad window error */ }
    }

    private fun updateLimitUI() {
        if (isFinishing || isDestroyed) return
        try {
            val count = prefs.getGenerationCount()
            val remaining = maxOf(0, Constants.FREE_GENERATION_LIMIT - count)
            if (prefs.isUnlocked()) {
                binding.tvLimitInfo.text = "✅ Unlimited access active"
                binding.btnWatchAd.hide()
            } else {
                binding.tvLimitInfo.text = "⚡ $remaining free generation${if (remaining != 1) "s" else ""} remaining"
                binding.btnWatchAd.show()
            }
        } catch (e: Exception) { /* view not ready */ }
    }

    private fun observeViewModel() {
        viewModel.seoState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> setLoadingState(true)
                is UiState.Success -> {
                    setLoadingState(false)
                    val topic = binding.etTopic.text?.toString()?.trim() ?: ""
                    prefs.incrementCount()
                    maybeShowInterstitial()
                    updateLimitUI()

                    try {
                        prefs.saveHistory(
                            HistoryItem(
                                type = Constants.TYPE_SEO,
                                topic = topic,
                                resultJson = gson.toJson(state.data)
                            )
                        )
                    } catch (e: Exception) { /* history save failure is non-fatal */ }

                    if (!isFinishing && !isDestroyed) {
                        val intent = Intent(this, SeoResultActivity::class.java).apply {
                            putExtra(Constants.EXTRA_SEO_CONTENT, state.data)
                            putExtra(Constants.EXTRA_TOPIC, topic)
                        }
                        startActivity(intent)
                    }
                    viewModel.resetSeo()
                }
                is UiState.Error -> {
                    setLoadingState(false)
                    showError(state.message)
                }
                is UiState.Idle -> setLoadingState(false)
            }
        }

        viewModel.shortsState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> setLoadingState(true)
                is UiState.Success -> {
                    setLoadingState(false)
                    val topic = binding.etTopic.text?.toString()?.trim() ?: ""
                    prefs.incrementCount()
                    maybeShowInterstitial()
                    updateLimitUI()

                    try {
                        prefs.saveHistory(
                            HistoryItem(
                                type = Constants.TYPE_SHORTS,
                                topic = topic,
                                resultJson = gson.toJson(state.data)
                            )
                        )
                    } catch (e: Exception) { /* non-fatal */ }

                    if (!isFinishing && !isDestroyed) {
                        val intent = Intent(this, ShortsResultActivity::class.java).apply {
                            putExtra(Constants.EXTRA_SHORTS_TITLES, state.data)
                            putExtra(Constants.EXTRA_TOPIC, topic)
                        }
                        startActivity(intent)
                    }
                    viewModel.resetShorts()
                }
                is UiState.Error -> {
                    setLoadingState(false)
                    showError(state.message)
                }
                is UiState.Idle -> setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(loading: Boolean) {
        if (isFinishing || isDestroyed) return
        binding.loadingLayout.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnGenerateSeo.isEnabled = !loading
        binding.btnGenerateShorts.isEnabled = !loading
    }

    private fun showError(msg: String) {
        if (isFinishing || isDestroyed) return
        try {
            MaterialAlertDialogBuilder(this)
                .setTitle("❌ Error")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        try {
            menu.findItem(R.id.action_dark_mode)?.title =
                if (prefs.isDarkMode()) "☀️ Light Mode" else "🌙 Dark Mode"
        } catch (e: Exception) { /* non-fatal */ }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            R.id.action_dark_mode -> {
                val newMode = !prefs.isDarkMode()
                prefs.setDarkMode(newMode)
                AppCompatDelegate.setDefaultNightMode(
                    if (newMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
                true
            }
            R.id.action_privacy -> {
                startActivity(Intent(this, PrivacyPolicyActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        interstitialAd = null
        rewardedAd = null
    }
}
