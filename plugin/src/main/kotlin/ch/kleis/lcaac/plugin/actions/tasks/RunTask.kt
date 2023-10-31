package ch.kleis.lcaac.plugin.actions.tasks

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.plugin.MyBundle
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
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File
import kotlin.io.path.Path

private const val greenTick = "\u2705"
private const val redCross = "\u274C"

class RunTask(
    private val project: Project,
    private val run: LcaRun,
    private val logger: TaskLogger,
    runnerName: String
) : Task.Backgroundable(project, runnerName) {
    companion object {
        private val LOG = Logger.getInstance(RunTask::class.java)
    }

    // TODO : composite ProgressIndicator
    override fun run(indicator: ProgressIndicator) {
        val size = run.runnableList.size
        run.runnableList.forEachIndexed { index, element ->

            element.generate?.let { generate(it, indicator) }
            element.assess?.let { assess(it, indicator) }
            element.tests?.let { tests(indicator) }
            element.execute?.let { execute(it, indicator) }

            indicator.fraction = index.toDouble() / size
            refreshFSAndWait()
        }


    }

    private fun refreshFSAndWait() {
        ApplicationManager.getApplication().invokeAndWait { ->
            val vFile = VfsUtil.findFile(
                Path(project.basePath!!), true
            )
            VirtualFileManager.getInstance().syncRefresh()
            vFile?.refresh(false, false)
            LOG.info("Now refresh")
            val dumbSrv = DumbService.getInstance(project)
            dumbSrv.completeJustSubmittedTasks()
        }
    }

    private fun tests(indicator: ProgressIndicator) {
        fun onTestSuccess(task: RunAllTestsTask) {
            val nbPassed = task.results.filter { it.isSuccess() }.size
            val nbFailed = task.results.size - nbPassed
            logger.info("Run All tests", "$nbPassed passed / $nbFailed failed")
            for (result in task.results) {
                val tick = if (result.isSuccess()) greenTick else redCross
                logger.info("  ", "$tick ${result.name}")
                for (assert in result.results) {
                    val assessTick = if (assert.isSuccess()) greenTick else redCross
                    logger.info("    ", "$assessTick ${assert.toString()}")
                }
            }
        }

        val task = RunAllTestsTask(project = project, ::onTestSuccess)
        task.run(indicator)
        if (task.results.all { it.isSuccess() }) {
            task.onSuccess()
        } else {
            logger.error("Tests Task ${run.runRef.name} failed", "Some tests failed during execution")
            throw ExecutionFailure("Tests Task ${run.runRef.name} failed. Some tests failed during execution")
        }
    }

    private fun generate(generate: LcaGenerate, indicator: ProgressIndicator) {
        var task: ModelGenerationTask? = null
        ApplicationManager.getApplication().runReadAction {
            val process = generate.getProcessRef().reference.resolve() as LcaProcess
            val file = process.containingFile as LcaFile?
            val containingDirectory = file?.containingDirectory

            containingDirectory?.let {
                task = ModelGenerationTask(
                    project = project,
                    process = process,
                    processName = process.name,
                    file = run.containingFile as LcaFile,
                    containingDirectory = it,
                    logger = logger
                )
            }
        }
        task?.run(indicator)
    }

    private fun execute(exec: LcaExecute, indicator: ProgressIndicator) {
        val cmds = exec.getCommands()
        logger.info("Execute ", cmds.joinToString(" "))
        val result = ProcessBuilder(cmds)
            .directory(File(project.basePath!!))
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor()
        if (result == 0) {
            logger.info("Execution", "succeed")
        } else {
            logger.error("Execution Failed", "exit code is $result")
            throw ExecutionFailure("Execution Failed, exit code is $result")
        }
    }

    private fun assess(assess: LcaAssess, indicator: ProgressIndicator) {
        var task: ContributionAnalysisTask? = null
        ApplicationManager.getApplication().runReadAction {
            val processTemplateSpec = assess.getProcessTemplateSpecRef() as LcaProcessTemplateSpec
            val process = assess.getProcessRef().reference.resolve() as LcaProcess
            val file = process.containingFile as LcaFile?
            val labelList = processTemplateSpec.matchLabels?.labelSelectorList ?: emptyList()
            val mapper = LcaMapper(BasicOperations)
            val labels = labelList.associate { selector ->
                selector.labelRef.name to mapper.dataExpression(selector.dataExpression).toString()
            }

            fun assesOnSuccess(task: ContributionAnalysisTask) {
                saveInventory(task.project, task.data!!.first, task.data!!.second, process.name)
            }

            file?.let {
                task = ContributionAnalysisTask(
                    project = project,
                    processName = process.name,
                    file = file,
                    matchLabels = labels,
                    success = ::assesOnSuccess
                )
            }
            task?.run(indicator)
            task?.onSuccess()
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
            val task = SaveTableModelTask(project, model, path.toFile(), logger)
            ProgressManager.getInstance().run(task)
            VirtualFileManager.getInstance().refreshAndFindFileByNioPath(path)
            LOG.info("File $path generated")
        }
    }

    override fun onSuccess() {
        logger.info("Run ${run.runRef.name}", "Success")
        val message = MyBundle.message("lca.task.tests.result", run.runnableList.size)
        NotificationGroupManager.getInstance()
            .getNotificationGroup("LcaAsCode")
            .createNotification(title, message, NotificationType.INFORMATION)
            .notify(project)

    }


    override fun onThrowable(e: Throwable) {
        logger.error("Run ${run.runRef.name} finish with Errors", e.message ?: "unknown error")
        LOG.warn("Unable to process computation", e)
    }
}

class ExecutionFailure(msg: String) : Throwable(msg) {

}

