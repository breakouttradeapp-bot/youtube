package com.aitube.seogenerator.activities

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aitube.seogenerator.databinding.ActivitySeoResultBinding
import com.aitube.seogenerator.models.SeoContent
import com.aitube.seogenerator.utils.*
import com.google.android.gms.ads.AdRequest

class SeoResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeoResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeoResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "SEO Results"

        val seoContent: SeoContent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Constants.EXTRA_SEO_CONTENT, SeoContent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Constants.EXTRA_SEO_CONTENT)
        }

        val topic = intent.getStringExtra(Constants.EXTRA_TOPIC).orEmpty()
        binding.tvTopic.text = if (topic.isNotEmpty()) "📌 Topic: $topic" else "📌 Generated SEO Content"

        if (seoContent != null) {
            bindContent(seoContent)
            animateCards()
        } else {
            showEmptyState()
        }

        try {
            binding.bannerAdView.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) { /* non-fatal */ }
    }

    private fun bindContent(seo: SeoContent) {
        binding.tvTitle.text = seo.title.ifEmpty { "—" }
        binding.tvDescription.text = seo.description.ifEmpty { "—" }
        binding.tvTags.text = seo.tags.ifEmpty { "—" }
        binding.tvHashtags.text = seo.hashtags.ifEmpty { "—" }

        binding.btnCopyTitle.setOnClickListener {
            if (seo.title.isNotEmpty()) copyToClipboard("Title", seo.title)
            else Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show()
        }
        binding.btnCopyDesc.setOnClickListener {
            if (seo.description.isNotEmpty()) copyToClipboard("Description", seo.description)
            else Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show()
        }
        binding.btnCopyTags.setOnClickListener {
            if (seo.tags.isNotEmpty()) copyToClipboard("Tags", seo.tags)
            else Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show()
        }
        binding.btnCopyHashtags.setOnClickListener {
            if (seo.hashtags.isNotEmpty()) copyToClipboard("Hashtags", seo.hashtags)
            else Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show()
        }

        val fullContent = buildString {
            if (seo.title.isNotEmpty()) append("🎬 Title:\n${seo.title}\n\n")
            if (seo.description.isNotEmpty()) append("📝 Description:\n${seo.description}\n\n")
            if (seo.tags.isNotEmpty()) append("🏷️ Tags:\n${seo.tags}\n\n")
            if (seo.hashtags.isNotEmpty()) append("#️⃣ Hashtags:\n${seo.hashtags}")
        }

        binding.btnShareAll.setOnClickListener {
            if (fullContent.isNotBlank()) shareText(fullContent)
            else Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEmptyState() {
        binding.tvTitle.text = "Content not available"
        binding.tvDescription.text = "Please go back and generate content again."
        binding.tvTags.text = "—"
        binding.tvHashtags.text = "—"
    }

    private fun animateCards() {
        try {
            binding.cardTitle.animateIn(100)
            binding.cardDescription.animateIn(200)
            binding.cardTags.animateIn(300)
            binding.cardHashtags.animateIn(400)
        } catch (e: Exception) { /* non-fatal */ }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
