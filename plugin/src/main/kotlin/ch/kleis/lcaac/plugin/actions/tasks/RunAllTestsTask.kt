package ch.kleis.lcaac.plugin.actions.tasks

import ch.kleis.lcaac.plugin.language.psi.stub.test.TestStubKeyIndex
import ch.kleis.lcaac.plugin.psi.LcaTest
import ch.kleis.lcaac.plugin.testing.LcaTestResult
import ch.kleis.lcaac.plugin.testing.LcaTestRunner
import ch.kleis.lcaac.plugin.ui.toolwindow.test_results.TestResultsWindow
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class RunAllTestsTask(
    project: Project,
    private val fetchTests: () -> Collection<LcaTest> = { runReadAction { TestStubKeyIndex.findAllTests(project) } },
) : Task.Backgroundable(project, "Run all tests") {
    private var results: ArrayList<LcaTestResult> = arrayListOf()

    companion object {
        private val LOG = Logger.getInstance(RunAllTestsTask::class.java)
    }


    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = true
        indicator.text = "Collecting tests ..."
        val runner = LcaTestRunner(project)
        val tests = fetchTests()
        tests.forEachIndexed { index, test ->
            indicator.fraction = index.toDouble() / tests.size
            indicator.text = "Running tests [$index/${tests.size}]"
            val result = runner.run(test)
            results.add(result)
        }
    }

    override fun onSuccess() {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Tests") ?: return
        val testResultsContent = TestResultsWindow(results).getContent()
        val content = ContentFactory.getInstance().createContent(
            testResultsContent,
            "All Tests",
            false,
        )
        toolWindow.contentManager.removeAllContents(true)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }

    override fun onThrowable(e: Throwable) {
        val title = "Error"
        NotificationGroupManager.getInstance()
            .getNotificationGroup("LcaAsCode")
            .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
            .notify(project)
        LOG.warn("Unable to process computation", e)
    }

}
