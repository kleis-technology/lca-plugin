package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.plugin.actions.csv.CsvProcessor
import ch.kleis.lcaac.plugin.actions.csv.CsvRequestReader
import ch.kleis.lcaac.plugin.actions.csv.CsvResultWriter
import ch.kleis.lcaac.plugin.language.loader.LcaFileCollector
import ch.kleis.lcaac.plugin.language.loader.LcaLoader
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File
import java.io.FileNotFoundException
import kotlin.io.path.Path

class ContributionAnalysisWithDataAction(
    private val processName: String,
    private val matchLabels: Map<String, String>,
) : AnAction(
    AllIcons.Actions.Execute,
) {
    companion object {
        private val LOG = Logger.getInstance(ContributionAnalysisWithDataAction::class.java)
    }

    init {
        this.templatePresentation.setText("Assess with ${processName}.csv", false)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val containingDirectory = file.containingDirectory ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Run with ${processName}.csv") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    // read
                    indicator.fraction = 0.0
                    indicator.text = "Reading $processName.csv"
                    val csvFile = Path(containingDirectory.virtualFile.path, "$processName.csv").toFile()
                    val requests = csvFile.inputStream().use {
                        val requestReader = CsvRequestReader(processName, matchLabels, it)
                        requestReader.read()
                    }

                    // process
                    val symbolTable = runReadAction {
                        val collector = LcaFileCollector(file.project)
                        val parser = LcaLoader(collector.collect(file), BasicOperations)
                        parser.load()
                    }
                    val csvProcessor = CsvProcessor(project, symbolTable)
                    val results = requests.flatMap { request ->
                        ProgressManager.checkCanceled()
                        indicator.text = "Processing using ${request.arguments()}"
                        indicator.fraction += 1.0 / requests.size
                        csvProcessor.process(request)
                    }

                    // write
                    indicator.text = "Writing to $processName.results.csv"
                    indicator.fraction = 1.0
                    val path = Path(containingDirectory.virtualFile.path, "$processName.results.csv")
                    val csvResultFile = path.toFile()
                    CsvResultWriter(csvResultFile.outputStream()).use { writer ->
                        writer.write(results)
                    }

                    // done
                    indicator.text = "Written to $processName.results.csv"
                    indicator.fraction = 1.0
                    val title = "${requests.size} successful assessments of process $processName"
                    val message = "Results stored in ${processName}.results.csv"
                    VirtualFileManager.getInstance().refreshAndFindFileByNioPath(path)
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
        })
    }
}
