package au.com.pbizannes.headlines

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeadlinesComposeEspressoFlowTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun waitAMoment(millis: Long = 1500) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @Test
    fun headlinesScreen_canOpenArticleInWebView() {
        composeTestRule.waitForIdle()

        waitAMoment(1000)
        composeTestRule
            .onAllNodesWithTag("headlines_list_item")
            .onFirst()
            .performClick()

        waitAMoment(1000)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("webview").assertExists()
    }

    @Test
    fun navigateThroughBottomNavigationBar_andVerifyScreenTitles() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(context.getString(R.string.bottom_nav_sources)).performClick()

        composeTestRule.onNodeWithTag(context.getString(R.string.sources_title)).assertExists()

        composeTestRule.onNodeWithText(context.getString(R.string.bottom_nav_saved)).performClick()

        composeTestRule.onNodeWithTag(context.getString(R.string.saved_articles_title)).assertExists()

        composeTestRule.onNodeWithText(context.getString(R.string.bottom_nav_headlines)).performClick()
    }
}
