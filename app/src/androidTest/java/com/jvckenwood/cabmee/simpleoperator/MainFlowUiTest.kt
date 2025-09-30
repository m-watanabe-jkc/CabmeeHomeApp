package com.jvckenwood.cabmee.homeapp

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jvckenwood.cabmee.homeapp.presentation.view.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import java.io.File

@RunWith(AndroidJUnit4::class)
class MainFlowUiTest {

    private val composeRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> =
        createAndroidComposeRule()

    private val dataStoreCleanupRule = DataStoreCleanupRule()

    @get:Rule
    val ruleChain: TestRule = RuleChain.outerRule(dataStoreCleanupRule).around(composeRule)

    @Test
    fun splashNavigatesToLogin() {
        composeRule.onNodeWithText("SPLASH SCREEN").assertIsDisplayed()
        composeRule.onNodeWithText("TO LOGIN SCREEN").performClick()

        waitForText("LOGIN SCREEN")
        composeRule.onNodeWithText("LOGIN SCREEN").assertIsDisplayed()
    }

    @Test
    fun loginToMainAndOperateCounter() {
        composeRule.onNodeWithText("TO LOGIN SCREEN").performClick()
        waitForText("LOGIN SCREEN")

        composeRule.onNodeWithText("LOGIN").performClick()

        waitForText("MAIN SCREEN (COUNT : 0)")
        composeRule.onNodeWithText("MAIN SCREEN (COUNT : 0)").assertIsDisplayed()

        composeRule.onNode(hasText("+", substring = true)).performClick()
        waitForText("MAIN SCREEN (COUNT : 1)")
        composeRule.onNodeWithText("MAIN SCREEN (COUNT : 1)").assertIsDisplayed()

        composeRule.onNodeWithText("RST").performClick()
        waitForText("MAIN SCREEN (COUNT : 0)")
        composeRule.onNodeWithText("MAIN SCREEN (COUNT : 0)").assertIsDisplayed()

        composeRule.onNodeWithText("LOGOUT").performClick()
        waitForText("LOGIN SCREEN")
        composeRule.onNodeWithText("LOGIN SCREEN").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(hasText(text))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private class DataStoreCleanupRule : TestRule {
        override fun apply(base: Statement, description: Description): Statement {
            return object : Statement() {
                override fun evaluate() {
                    try {
                        deleteDataStore()
                        base.evaluate()
                    } finally {
                        deleteDataStore()
                    }
                }
            }
        }

        private fun deleteDataStore() {
            val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
            val file = File(context.filesDir, "activate.pb")
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
