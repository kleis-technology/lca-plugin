package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.plugin.actions.tasks.RunTask
import ch.kleis.lcaac.plugin.actions.tasks.TaskLogger
import ch.kleis.lcaac.plugin.actions.tasks.TerminalTaskLogger
import ch.kleis.lcaac.plugin.psi.LcaRun
import com.intellij.execution.process.ProcessHandler
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.terminal.TerminalExecutionConsole
import com.intellij.ui.content.ContentFactory
import java.io.OutputStream

class RunRunAction(
    private val run: LcaRun,
    private val runnerName: String,
) : AnAction(
    "Run",
    "Run",
    AllIcons.Actions.Execute,
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val logger = createLogPanel(project)
        val task = RunTask(project, run, logger, runnerName)

        ProgressManager.getInstance().run(task)
    }

    private fun createLogPanel(project: Project): TaskLogger {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Run")
            ?: throw Error("Unable to find Run Logger panel")
        val handler = object : ProcessHandler() {
            override fun destroyProcessImpl() {
                TODO("Not yet implemented")
            }

            override fun detachProcessImpl() {
                TODO("Not yet implemented")
            }

            override fun detachIsDefault(): Boolean {
                TODO("Not yet implemented")
            }

            override fun getProcessInput(): OutputStream? {
                TODO("Not yet implemented")
            }

        }
        val termContent = TerminalExecutionConsole(project, handler)
//        val testResultsContent = TestResultsWindow(results).getContent()
        val content = ContentFactory.getInstance().createContent(
            termContent.terminalWidget,
            "Run $runnerName",
            false,
        )
        toolWindow.contentManager.removeAllContents(true)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
        return TerminalTaskLogger(termContent)
    }

}

