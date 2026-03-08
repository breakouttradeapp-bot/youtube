package com.aitube.seogenerator.activities

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aitube.seogenerator.databinding.ActivityShortsResultBinding
import com.aitube.seogenerator.databinding.ItemShortsTitleBinding
import com.aitube.seogenerator.models.ShortsTitles
import com.aitube.seogenerator.utils.*
import com.google.android.gms.ads.AdRequest

class ShortsResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShortsResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShortsResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "🎬 Viral Shorts Titles"

        val shorts: ShortsTitles? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Constants.EXTRA_SHORTS_TITLES, ShortsTitles::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Constants.EXTRA_SHORTS_TITLES)
        }

        val topic = intent.getStringExtra(Constants.EXTRA_TOPIC).orEmpty()
        binding.tvTopic.text = if (topic.isNotEmpty()) "📌 Topic: $topic" else "📌 Generated Titles"

        val titles = shorts?.titles?.filter { it.isNotBlank() } ?: emptyList()
        setupRecycler(titles)

        try {
            binding.bannerAdView.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) { /* non-fatal */ }

        val shareContent = titles.mapIndexed { i, t -> "${i + 1}. $t" }.joinToString("\n")
        binding.btnShareAll.setOnClickListener {
            if (shareContent.isNotBlank()) shareText("🚀 Viral YouTube Shorts Titles:\n\n$shareContent")
            else Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecycler(titles: List<String>) {
        binding.recyclerTitles.layoutManager = LinearLayoutManager(this)
        binding.recyclerTitles.adapter = TitlesAdapter(titles) { title ->
            if (title.isNotBlank()) copyToClipboard("Shorts Title", title)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

class TitlesAdapter(
    private val titles: List<String>,
    private val onCopy: (String) -> Unit
) : RecyclerView.Adapter<TitlesAdapter.VH>() {

    inner class VH(val binding: ItemShortsTitleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemShortsTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (position < 0 || position >= titles.size) return
        val title = titles[position]
        holder.binding.tvNumber.text = "${position + 1}"
        holder.binding.tvTitle.text = title
        holder.binding.btnCopy.setOnClickListener { onCopy(title) }
        try { holder.itemView.animateIn(minOf(position * 80L, 600L)) } catch (e: Exception) {}
    }

    override fun getItemCount(): Int = titles.size
}
