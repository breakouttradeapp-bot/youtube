package com.aitube.seogenerator.utils

object Constants {

    // ── Cerebras AI API ───────────────────────────────────────────────────────
    const val CEREBRAS_API_KEY = "csk-kvpefvxk65wdy5x4ek2ekw3enehd62x4eyfccpjx86n2e8d6"

    // Confirmed working model IDs from Cerebras official docs
    const val CEREBRAS_MODEL          = "llama-3.3-70b"   // primary  (with dash)
    const val CEREBRAS_MODEL_FALLBACK = "llama3.1-8b"      // fallback (no dash after llama)

    const val MAX_TOKENS = 1024

    // ── AdMob Test IDs ────────────────────────────────────────────────────────
    const val ADMOB_BANNER_ID       = "ca-app-pub-3940256099942544/6300978111"
    const val ADMOB_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val ADMOB_REWARDED_ID     = "ca-app-pub-3940256099942544/5224354917"

    // ── Generation limits ─────────────────────────────────────────────────────
    const val FREE_GENERATION_LIMIT   = 5
    const val INTERSTITIAL_EVERY_N    = 3
    const val REWARDED_UNLOCK_MINUTES = 30L

    // ── SharedPrefs keys ──────────────────────────────────────────────────────
    const val PREFS_NAME       = "aitube_prefs"
    const val KEY_GEN_COUNT    = "generation_count"
    const val KEY_UNLOCK_UNTIL = "unlock_until"
    const val KEY_HISTORY      = "history_list"
    const val KEY_DARK_MODE    = "dark_mode"

    // ── Intent extras ─────────────────────────────────────────────────────────
    const val EXTRA_SEO_CONTENT   = "seo_content"
    const val EXTRA_SHORTS_TITLES = "shorts_titles"
    const val EXTRA_TOPIC         = "topic"
    const val EXTRA_TYPE          = "type"
    const val TYPE_SEO            = "SEO"
    const val TYPE_SHORTS         = "SHORTS"
}

