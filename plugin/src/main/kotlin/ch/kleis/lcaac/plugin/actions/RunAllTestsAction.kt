package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.plugin.language.psi.stub.test.TestStubKeyIndex
import ch.kleis.lcaac.plugin.testing.LcaTestResult
import ch.kleis.lcaac.plugin.testing.LcaTestRunner
import ch.kleis.lcaac.plugin.ui.toolwindow.test_results.TestResultsWindow
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class RunAllTestsAction : AnAction(
    "Run All Tests",
    "Run all tests",
    AllIcons.Actions.Execute,
) {
    companion object {
        private val LOG = Logger.getInstance(RunAllTestsAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Run All Tests") {
            private var results: ArrayList<LcaTestResult> = arrayListOf()
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Collecting tests ..."
                val tests = runReadAction {  TestStubKeyIndex.findAllTests(project) }
                tests.forEachIndexed { index, test ->
                    indicator.fraction = index.toDouble() / tests.size
                    indicator.text = "Running tests [$index/${tests.size}]"
                    val runner = LcaTestRunner(project)
                    val result = runner.run(test)
                    results.add(result)
                }
            }

            override fun onSuccess() {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
                val testResultsContent = TestResultsWindow(results).getContent()
                val content = ContentFactory.getInstance().createContent(
                    testResultsContent,
                    "All Tests",
                    false,
                )
                toolWindow.contentManager.addContent(content)
                toolWindow.contentManager.setSelectedContent(content)
                toolWindow.show()
            }
        })
    }
}
