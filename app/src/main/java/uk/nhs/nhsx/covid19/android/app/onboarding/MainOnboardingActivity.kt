package uk.nhs.nhsx.covid19.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main_onboarding.confirmOnboarding
import uk.nhs.nhsx.covid19.android.app.R

class MainOnboardingActivity : AppCompatActivity(R.layout.activity_main_onboarding) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        confirmOnboarding.setOnClickListener {
            DataAndPrivacyActivity.start(this)
        }
    }

    companion object {
        fun start(context: Context) =
            context.startActivity(getIntent(context))

        private fun getIntent(context: Context) =
            Intent(context, MainOnboardingActivity::class.java)
    }
}
