package uk.nhs.nhsx.covid19.android.app.testordering

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_test_ordering_progress.buttonTryAgain
import kotlinx.android.synthetic.main.activity_test_ordering_progress.errorStateContainer
import kotlinx.android.synthetic.main.activity_test_ordering_progress.loadingProgress
import kotlinx.android.synthetic.main.view_toolbar_primary.toolbar
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.appComponent
import uk.nhs.nhsx.covid19.android.app.common.Lce.Error
import uk.nhs.nhsx.covid19.android.app.common.Lce.Loading
import uk.nhs.nhsx.covid19.android.app.common.Lce.Success
import uk.nhs.nhsx.covid19.android.app.common.ViewModelFactory
import uk.nhs.nhsx.covid19.android.app.util.gone
import uk.nhs.nhsx.covid19.android.app.util.openUrl
import uk.nhs.nhsx.covid19.android.app.util.setNavigateUpToolbar
import uk.nhs.nhsx.covid19.android.app.util.visible
import javax.inject.Inject

class TestOrderingProgressActivity : AppCompatActivity(R.layout.activity_test_ordering_progress) {

    @Inject
    lateinit var factory: ViewModelFactory<TestOrderingProgressViewModel>

    private val viewModel: TestOrderingProgressViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)

        setNavigateUpToolbar(toolbar, R.string.empty, R.drawable.ic_close_primary)

        viewModel.loadVirologyTestOrder()
        setupListeners()
        setupViewModelListeners()
    }

    private fun setupListeners() {
        buttonTryAgain.setOnClickListener {
            viewModel.loadVirologyTestOrder()
        }
    }

    private fun setupViewModelListeners() {
        viewModel.websiteUrlWithQuery().observe(
            this,
            Observer {
                when (it) {
                    is Success -> {
                        openVirologyTestOrderWebsite(it.data)
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    is Error -> showErrorState()
                    is Loading -> showLoadingSpinner()
                }
            }
        )
    }

    private fun showLoadingSpinner() {
        errorStateContainer.gone()
        loadingProgress.visible()
    }

    private fun showErrorState() {
        errorStateContainer.visible()
        loadingProgress.gone()
    }

    private fun openVirologyTestOrderWebsite(websiteUrlWithQuery: String) {
        openUrl(websiteUrlWithQuery)
    }

    companion object {
        fun getIntent(context: Context) =
            Intent(context, TestOrderingProgressActivity::class.java)
    }
}
