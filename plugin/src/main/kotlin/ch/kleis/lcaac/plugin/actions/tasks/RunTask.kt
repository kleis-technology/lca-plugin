package ch.kleis.lcaac.plugin.actions.tasks

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.plugin.language.loader.LcaMapper
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.*
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.DemandTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.ImpactAssessmentTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.InventoryTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.SupplyTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.SaveTableModelTask
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import kotlin.io.path.Path

typealias Contrib = ContributionAnalysis<BasicNumber, BasicMatrix>

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

            element.generate?.let { generate(it) }
            element.assess?.let { assess(it) }

            indicator.fraction = index.toDouble() / size
        }


    }

    private fun generate(generate: LcaGenerate) {
        ApplicationManager.getApplication().runReadAction {
            val process = generate.getProcessRef().reference.resolve() as LcaProcess
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

    private fun assess(assess: LcaAssess) {
        ApplicationManager.getApplication().runReadAction {
            val processTemplateSpec = assess.getProcessTemplateSpecRef() as LcaProcessTemplateSpec
            val process = assess.getProcessRef().reference.resolve() as LcaProcess
            val file = process.containingFile as LcaFile?
            val containingDirectory = file?.containingDirectory
            val labelList = processTemplateSpec.matchLabels?.labelSelectorList ?: emptyList()
            val mapper = LcaMapper(BasicOperations)
            val labels =
                labelList.associate { selector ->
                    selector.labelRef.name to mapper.dataExpression(selector.dataExpression).toString()
                }

            fun onSuccess(
                p: Project,
                a: ContributionAnalysis<BasicNumber, BasicMatrix>,
                c: Comparator<MatrixColumnIndex<BasicNumber>>
            ) {
                saveInventory(p, a, c, process.name)
            }

            containingDirectory?.let {
                val task = ContributionAnalysisTask(
                    project = project,
                    processName = process.name,
                    file = file,
                    matchLabels = labels,
                    success = ::onSuccess
                )
                ProgressManager.getInstance().run(task)
            }
        }
    }

    private fun saveInventory(
        project: Project,
        analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
        comparator: Comparator<MatrixColumnIndex<BasicNumber>>,
        processName: String
    ) {
        val nonCharacterizedSubstances = analysis.getNonCharacterizedSubstances()
        val unresolvedProducts = analysis.getUnresolvedProducts()
        val output = listOf(
            "demand" to DemandTableModel(analysis),
            "impact_assessment.csv" to ImpactAssessmentTableModel(analysis),
            "inventory" to InventoryTableModel(analysis, comparator),
            "supply" to SupplyTableModel(analysis, comparator),
            "issue-product-without-process" to SupplyTableModel(analysis, comparator, unresolvedProducts),
            "issue-product-without-impact" to InventoryTableModel(analysis, comparator, nonCharacterizedSubstances)
        )
        output.forEach { (file, model) ->
            val path = Path("${project.basePath}/out/${processName}-$file.csv")
            val task = SaveTableModelTask(project, model, path.toFile())
            ProgressManager.getInstance().run(task)
            VirtualFileManager.getInstance().refreshAndFindFileByNioPath(path)
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

