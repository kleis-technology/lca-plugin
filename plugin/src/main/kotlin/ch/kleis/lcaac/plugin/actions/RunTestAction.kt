package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.testing.LcaTestResult
import ch.kleis.lcaac.plugin.testing.LcaTestRunner
import ch.kleis.lcaac.plugin.ui.toolwindow.test_results.TestResultsWindow
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class RunTestAction(
    private val testName: String,
) : AnAction(
    "Run",
    "Run",
    AllIcons.Actions.Execute,
) {
    companion object {
        private val LOG = Logger.getInstance(RunTestAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val test = file.findTest(testName) ?: return
        val runner = LcaTestRunner(project)
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Run") {
            private var result: LcaTestResult? = null
            override fun run(indicator: ProgressIndicator) {
                this.result = runner.run(test)
            }

            override fun onSuccess() {
                this.result?.let {
                    displayTestResult(it)
                }
            }

            override fun onThrowable(e: Throwable) {
                val title = "Error while running $testName"
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("LcaAsCode")
                    .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                    .notify(project)
                LOG.warn("Unable to process computation", e)
            }

            private fun displayTestResult(it: LcaTestResult) {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
                val testResultsContent = TestResultsWindow(listOf(it)).getContent()
                val content = ContentFactory.getInstance().createContent(
                    testResultsContent,
                    "Test $testName",
                    false,
                )
                toolWindow.contentManager.addContent(content)
                toolWindow.contentManager.setSelectedContent(content)
                toolWindow.show()
            }
        })
    }
}
