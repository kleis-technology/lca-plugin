package ch.kleis.lcaac.plugin.actions.tasks

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.LcaProcess
import ch.kleis.lcaac.plugin.psi.LcaRun
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

class RunTask(
    project: Project,
    private val run: LcaRun,
    runnerName: String,
) : Task.Backgroundable(project, runnerName) {
    companion object {
        private val LOG = Logger.getInstance(RunTask::class.java)
    }

    override fun run(indicator: ProgressIndicator) {
        val size = run.runnableList.size
        run.runnableList.forEachIndexed { index, element ->

            element.assess?.let { assess ->
//                val assess = element.assess
                ApplicationManager.getApplication().runReadAction {
                    val process = assess.getProcessRef().reference.resolve() as LcaProcess
//                    val process = processRef.reference.resolve()
                    val file = process.containingFile as LcaFile?
                    val containingDirectory = file?.containingDirectory
                    containingDirectory?.let {
                        val task = ModelGenerationTask(
                            project = project,
                            process = process,
                            processName = process.name,
                            file = run.containingFile as LcaFile,
                            containingDirectory = it
                        )
                        ProgressManager.getInstance().run(task)
                    }
                }
            }

            indicator.fraction = index.toDouble() / size
        }


    }

    override fun onSuccess() {
//        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Tests") ?: return
//        val testResultsContent = TestResultsWindow(results).getContent()
//        val content = ContentFactory.getInstance().createContent(
//            testResultsContent,
//            "All Tests",
//            false,
//        )
//        toolWindow.contentManager.removeAllContents(true)
//        toolWindow.contentManager.addContent(content)
//        toolWindow.contentManager.setSelectedContent(content)
//        toolWindow.show()
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