package com.aitube.seogenerator.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aitube.seogenerator.databinding.ActivityHistoryBinding
import com.aitube.seogenerator.databinding.ItemHistoryBinding
import com.aitube.seogenerator.models.HistoryItem
import com.aitube.seogenerator.models.SeoContent
import com.aitube.seogenerator.models.ShortsTitles
import com.aitube.seogenerator.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var prefs: PrefsManager
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "📋 History"
        prefs = PrefsManager(this)
        loadHistory()

        binding.btnClearHistory.setOnClickListener {
            if (isFinishing || isDestroyed) return@setOnClickListener
            try {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Clear History")
                    .setMessage("Delete all saved generations?")
                    .setPositiveButton("Clear") { _, _ ->
                        prefs.clearHistory()
                        loadHistory()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } catch (e: Exception) {
                prefs.clearHistory()
                loadHistory()
            }
        }
    }

    private fun loadHistory() {
        if (isFinishing || isDestroyed) return
        val history = try { prefs.getHistory() } catch (e: Exception) { emptyList() }
        if (history.isEmpty()) {
            binding.tvEmpty.show()
            binding.recyclerHistory.hide()
            binding.btnClearHistory.hide()
        } else {
            binding.tvEmpty.hide()
            binding.recyclerHistory.show()
            binding.btnClearHistory.show()
            binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
            binding.recyclerHistory.adapter = HistoryAdapter(history) { openItemSafely(it) }
        }
    }

    private fun openItemSafely(item: HistoryItem) {
        if (isFinishing || isDestroyed) return
        try {
            if (item.type == Constants.TYPE_SEO) {
                val seo = gson.fromJson(item.resultJson, SeoContent::class.java) ?: SeoContent()
                startActivity(
                    Intent(this, SeoResultActivity::class.java).apply {
                        putExtra(Constants.EXTRA_SEO_CONTENT, seo)
                        putExtra(Constants.EXTRA_TOPIC, item.topic)
                    }
                )
            } else {
                val shorts = gson.fromJson(item.resultJson, ShortsTitles::class.java) ?: ShortsTitles()
                startActivity(
                    Intent(this, ShortsResultActivity::class.java).apply {
                        putExtra(Constants.EXTRA_SHORTS_TITLES, shorts)
                        putExtra(Constants.EXTRA_TOPIC, item.topic)
                    }
                )
            }
        } catch (e: JsonSyntaxException) {
            Toast.makeText(this, "Could not open this item.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening item.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

class HistoryAdapter(
    private val items: List<HistoryItem>,
    private val onClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    private val fmt = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())

    inner class VH(val b: ItemHistoryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (position < 0 || position >= items.size) return
        val item = items[position]
        holder.b.tvTopic.text = item.topic.ifEmpty { "Unknown topic" }
        holder.b.tvType.text = if (item.type == Constants.TYPE_SEO) "🔍 SEO Content" else "🎬 Shorts Titles"
        holder.b.tvDate.text = try { fmt.format(Date(item.timestamp)) } catch (e: Exception) { "" }
        holder.itemView.setOnClickListener { onClick(item) }
        try { holder.itemView.animateIn(minOf(position * 60L, 500L)) } catch (e: Exception) {}
    }

    override fun getItemCount(): Int = items.size
}
