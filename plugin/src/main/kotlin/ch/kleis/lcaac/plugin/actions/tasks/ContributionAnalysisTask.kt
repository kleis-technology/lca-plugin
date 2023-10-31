package ch.kleis.lcaac.plugin.actions.tasks

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.plugin.actions.traceSystemWithIndicator
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

typealias OnSuccess<T> = (task: T) -> Unit

class ContributionAnalysisTask(
    project: Project,
    private val processName: String,
    private val file: LcaFile,
    private val matchLabels: Map<String, String>,
    private val success: OnSuccess<ContributionAnalysisTask>
) : Task.Backgroundable(project, "Run") {
    companion object {
        private val LOG = Logger.getInstance(ContributionAnalysisTask::class.java)
    }

    var data: Pair<ContributionAnalysis<BasicNumber, BasicMatrix>, Comparator<MatrixColumnIndex<BasicNumber>>>? =
        null

    override fun run(indicator: ProgressIndicator) {
        val trace = traceSystemWithIndicator(indicator, file, processName, matchLabels, BasicOperations)
        val comparator = trace.getComparator()
        val analysis = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint()).run()
        this.data = Pair(analysis, comparator)
    }

    override fun onSuccess() {
        this.data?.let {
            success(this)
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


}
