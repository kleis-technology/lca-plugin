package ch.kleis.lcaac.plugin.actions.tasks

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.plugin.actions.csv.CsvRequest
import ch.kleis.lcaac.plugin.actions.csv.CsvRequestReader
import ch.kleis.lcaac.plugin.actions.csv.OutputToModelWriter
import ch.kleis.lcaac.plugin.language.loader.LcaFileCollector
import ch.kleis.lcaac.plugin.language.loader.LcaLoader
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.LcaProcess
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDirectory
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.Path

class ModelGenerationTask(
    project: Project,
    private val process: LcaProcess,
    private val processName: String,
    private val file: LcaFile,
    private val containingDirectory: PsiDirectory,
) : Task.Backgroundable(project, "Run with ${processName}.csv") {
    companion object {
        private val LOG = Logger.getInstance(ModelGenerationTask::class.java)
    }

    override fun run(indicator: ProgressIndicator) {

        try {
            // read
            indicator.fraction = 0.0
            indicator.text = "Reading $processName.csv"
            val csvFile = Path(containingDirectory.virtualFile.path, "$processName.csv").toFile()
            val requests = csvFile.inputStream().use {
                val requestReader = CsvRequestReader(processName, process.getLabels(), it)
                requestReader.read()
            }

            // process
            val symbolTable = runReadAction {
                val collector = LcaFileCollector(file.project)
                val parser = LcaLoader(collector.collect(file), BasicOperations)
                parser.load()
            }
//                    val csvProcessor = CsvProcessor(symbolTable)
//                    val results = requests.flatMap { request ->
//                        ProgressManager.checkCanceled()
//                        indicator.text = "Processing using ${request.arguments()}"
//                        indicator.fraction += 1.0 / requests.size
//                        csvProcessor.process(request)
//                    }

            // write
            val outputPath = writeModel(indicator, containingDirectory, requests)

            // TODO Refactor
            // done
            indicator.text = "Written to $processName.results.csv"
            indicator.fraction = 1.0
            val title = "${requests.size} successful assessments of process $processName"
            val message = "Results stored in ${processName}.results.csv"
            VirtualFileManager.getInstance().refreshAndFindFileByNioPath(outputPath)
            NotificationGroupManager.getInstance()
                .getNotificationGroup("LcaAsCode")
                .createNotification(title, message, NotificationType.INFORMATION)
                .notify(project)
        } catch (e: EvaluatorException) {
            val title = "Error while assessing $processName"
            NotificationGroupManager.getInstance()
                .getNotificationGroup("LcaAsCode")
                .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                .notify(project)
            LOG.warn("Unable to process computation", e)
        } catch (e: NoSuchElementException) {
            val title = "Error while assessing $processName"
            NotificationGroupManager.getInstance()
                .getNotificationGroup("LcaAsCode")
                .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                .notify(project)
            LOG.warn("Unable to process computation", e)
        } catch (e: FileNotFoundException) {
            val title = "Error while assessing $processName"
            NotificationGroupManager.getInstance()
                .getNotificationGroup("LcaAsCode")
                .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                .notify(project)
        }
    }

    private fun writeModel(indicator: ProgressIndicator, outDir: PsiDirectory, results: List<CsvRequest>): Path {
        indicator.text = "Writing to $processName.results.csv"
        indicator.fraction = 1.0
        val path = Path(outDir.virtualFile.path, "$processName.generated.models.lca")
        OutputToModelWriter(process, results, path).writeModels()
        return path
    }

}