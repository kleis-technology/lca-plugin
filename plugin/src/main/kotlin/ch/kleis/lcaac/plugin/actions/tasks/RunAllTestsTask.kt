package ch.kleis.lcaac.plugin.actions.tasks

import ch.kleis.lcaac.plugin.language.psi.stub.test.TestStubKeyIndex
import ch.kleis.lcaac.plugin.psi.LcaTest
import ch.kleis.lcaac.plugin.testing.LcaTestResult
import ch.kleis.lcaac.plugin.testing.LcaTestRunner
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

class RunAllTestsTask(
    project: Project,
    private val success: OnSuccess<RunAllTestsTask>,
    private val fetchTests: () -> Collection<LcaTest> = { runReadAction { TestStubKeyIndex.findAllTests(project) } }
) : Task.Backgroundable(project, "Run all tests") {
    val results: ArrayList<LcaTestResult> = arrayListOf()

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
        success(this)
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
