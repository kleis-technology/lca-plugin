package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.plugin.actions.tasks.RunAllTestsTask
import ch.kleis.lcaac.plugin.ui.toolwindow.test_results.TestResultsWindow
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class RunAllTestsAction : AnAction(
    "Run All Tests",
    "Run all tests",
    AllIcons.Actions.Execute,
) {
    companion object {
        fun onTestSuccess(task: RunAllTestsTask) {
            val toolWindow = ToolWindowManager.getInstance(task.project).getToolWindow("LCA Tests") ?: return
            val testResultsContent = TestResultsWindow(task.results).getContent()
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
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val task = RunAllTestsTask(project, ::onTestSuccess)
        ProgressManager.getInstance().run(task)
    }


}
