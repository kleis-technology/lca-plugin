package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.ContributionAnalysisWindow
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
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class AssessProcessAction(
    private val processName: String,
    private val matchLabels: Map<String, String>,
) : AnAction(
    "Assess",
    "Assess",
    AllIcons.Actions.Execute,
) {
    companion object {
        private val LOG = Logger.getInstance(AssessProcessAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Run") {
            private var data: Pair<ContributionAnalysis<BasicNumber, BasicMatrix>, EvaluationTrace<BasicNumber>>? = null

            override fun run(indicator: ProgressIndicator) {
                val trace = traceSystemWithIndicator(indicator, file, processName, matchLabels, BasicOperations)
                val analysis = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint()).run()
                this.data = Pair(analysis, trace)
            }

            override fun onSuccess() {
                this.data?.let {
                    displayInventory(project, it.first, it.second)
                }
            }

            override fun onThrowable(e: Throwable) {
                val title = "Error while assessing $processName"
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("LcaAsCode")
                    .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                    .notify(project)
                LOG.warn("Unable to process computation", e)
            }

            private fun displayInventory(
                project: Project,
                analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
                trace: EvaluationTrace<BasicNumber>,
            ) {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
                val assessResultContent = ContributionAnalysisWindow(analysis, trace, project, processName).getContent()
                val content = ContentFactory.getInstance().createContent(
                    assessResultContent,
                    "Contribution analysis of $processName",
                    false,
                )
                toolWindow.contentManager.addContent(content)
                toolWindow.contentManager.setSelectedContent(content)
                toolWindow.show()
            }
        })
    }

}


