package uk.nhs.nhsx.covid19.android.app.questionnaire.selection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_questionnaire.buttonTryAgain
import kotlinx.android.synthetic.main.activity_questionnaire.errorStateContainer
import kotlinx.android.synthetic.main.activity_questionnaire.loadingContainer
import kotlinx.android.synthetic.main.activity_questionnaire.questionListContainer
import kotlinx.android.synthetic.main.include_show_questionnaire.buttonReviewSymptoms
import kotlinx.android.synthetic.main.include_show_questionnaire.errorPanel
import kotlinx.android.synthetic.main.include_show_questionnaire.questionsRecyclerView
import kotlinx.android.synthetic.main.include_show_questionnaire.textNoSymptoms
import kotlinx.android.synthetic.main.view_toolbar_primary.toolbar
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.appComponent
import uk.nhs.nhsx.covid19.android.app.common.Lce.Error
import uk.nhs.nhsx.covid19.android.app.common.Lce.Loading
import uk.nhs.nhsx.covid19.android.app.common.Lce.Success
import uk.nhs.nhsx.covid19.android.app.common.ViewModelFactory
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.NoSymptomsActivity
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.ReviewSymptomsActivity
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.ReviewSymptomsActivity.Companion.EXTRA_RISK_THRESHOLD
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.ReviewSymptomsActivity.Companion.EXTRA_SYMPTOMS_ONSET_WINDOW_DAYS
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.adapter.ReviewSymptomItem.Question
import uk.nhs.nhsx.covid19.android.app.questionnaire.selection.adapter.QuestionnaireAdapter
import uk.nhs.nhsx.covid19.android.app.startActivity
import uk.nhs.nhsx.covid19.android.app.util.gone
import uk.nhs.nhsx.covid19.android.app.util.setNavigateUpToolbar
import uk.nhs.nhsx.covid19.android.app.util.visible
import javax.inject.Inject

class QuestionnaireActivity : AppCompatActivity(R.layout.activity_questionnaire) {
    private lateinit var questionnaireAdapter: QuestionnaireAdapter

    @Inject
    lateinit var factory: ViewModelFactory<QuestionnaireViewModel>

    private val viewModel: QuestionnaireViewModel by viewModels { factory }

    private lateinit var nestedScrollView: NestedScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setNavigateUpToolbar(toolbar, R.string.select_symptoms, R.drawable.ic_close_white)

        appComponent.inject(this)

        nestedScrollView = questionListContainer as NestedScrollView

        setupListeners()
        setupAdapter()
        setupViewModelListeners()
        viewModel.loadQuestionnaire()
    }

    private fun setupViewModelListeners() {
        viewModel.viewState().observe(
            this,
            Observer {
                when (it) {
                    is Success -> handleSuccess(it.data)
                    is Error -> showErrorState()
                    is Loading -> showLoadingSpinner()
                }
            }
        )

        viewModel.navigateToReviewScreen().observe(
            this,
            Observer { questions ->
                val extraQuestions = ArrayList<Question>().apply {
                    addAll(questions)
                }

                val intent = Intent(this, ReviewSymptomsActivity::class.java).apply {
                    putParcelableArrayListExtra(
                        ReviewSymptomsActivity.EXTRA_QUESTIONS,
                        extraQuestions
                    )
                }.apply {
                    putExtra(EXTRA_RISK_THRESHOLD, viewModel.getRiskThreshold())
                    putExtra(
                        EXTRA_SYMPTOMS_ONSET_WINDOW_DAYS,
                        viewModel.getSymptomsOnsetWindowDays()
                    )
                }

                startActivityForResult(intent, CHANGE_QUESTION_REQUEST_CODE)
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == CHANGE_QUESTION_REQUEST_CODE) {
            val question = data?.getParcelableExtra<Question>(QUESTION_TO_CHANGE_KEY)

            val index = question?.let { questionnaireAdapter.currentList.indexOf(question) } ?: -1

            (questionsRecyclerView.layoutManager as ScrollableLayoutManager).scrollToIndex(index)
        }
    }

    private fun handleSuccess(state: QuestionnaireState) {
        showQuestionnaire(state.questions)
        if (state.showError) {
            errorPanel.visible()
            nestedScrollView.smoothScrollTo(0, 0)
            errorPanel.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        } else {
            errorPanel.gone()
        }
    }

    private fun setupListeners() {
        buttonTryAgain.setOnClickListener {
            viewModel.loadQuestionnaire()
        }

        textNoSymptoms.setOnClickListener {
            finish()
            startActivity<NoSymptomsActivity>()
        }

        buttonReviewSymptoms.setOnClickListener {
            viewModel.onButtonReviewSymptomsClicked()
        }
    }

    private fun showLoadingSpinner() {
        loadingContainer.visible()
        errorStateContainer.gone()
        questionListContainer.gone()
    }

    private fun showErrorState() {
        errorStateContainer.visible()
        loadingContainer.gone()
        questionListContainer.gone()
    }

    private fun showQuestionnaire(questions: List<Question>) {
        questionListContainer.visible()
        loadingContainer.gone()
        errorStateContainer.gone()
        submitQuestions(questions)
    }

    private fun submitQuestions(questions: List<Question>) {
        questionnaireAdapter.submitList(questions)
    }

    private fun setupAdapter() {
        questionnaireAdapter = QuestionnaireAdapter {
            viewModel.toggleQuestion(it)
        }
        questionsRecyclerView.layoutManager =
            ScrollableLayoutManager(this, nestedScrollView, questionsRecyclerView)
        questionsRecyclerView.adapter = questionnaireAdapter
    }

    companion object {
        private const val CHANGE_QUESTION_REQUEST_CODE = 12345
        const val QUESTION_TO_CHANGE_KEY = "QUESTION_TO_CHANGE_KEY"
    }
}
