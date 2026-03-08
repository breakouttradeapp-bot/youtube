package com.aitube.seogenerator.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aitube.seogenerator.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Privacy Policy"

        binding.tvPrivacy.text = getPrivacyText()
    }

    private fun getPrivacyText() = """
AI Tube SEO Generator — Privacy Policy
Last updated: January 2025

1. INFORMATION WE COLLECT
This app collects the following information:
• Video topic text that you enter (sent to AI API for content generation)
• Advertising ID used by Google AdMob for personalized ads

2. HOW WE USE YOUR INFORMATION
• Your video topic is sent to the Cerebras AI API solely to generate YouTube SEO content
• We do not store your topics on any server
• Generated content is saved locally on your device for history purposes only

3. THIRD-PARTY SERVICES
• Cerebras AI API: Processes your topic to generate SEO content
• Google AdMob: Displays advertisements. Google may collect data per their privacy policy
• See: https://policies.google.com/privacy

4. DATA STORAGE
• All history is stored locally on your device using SharedPreferences
• No personal data is stored on our servers

5. ADVERTISING
• This app uses Google AdMob for monetization
• Ads may be personalized based on your Advertising ID
• You can opt out of personalized ads in your device settings

6. PERMISSIONS
• INTERNET: Required to call the AI API and load advertisements
• ACCESS_NETWORK_STATE: Required to check network availability

7. CHILDREN'S PRIVACY
This app is not directed at children under 13. We do not knowingly collect data from children.

8. CHANGES TO THIS POLICY
We may update this policy. Changes will be posted within the app.

9. CONTACT
For questions about this privacy policy, please contact us through the app store listing.
    """.trimIndent()

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
