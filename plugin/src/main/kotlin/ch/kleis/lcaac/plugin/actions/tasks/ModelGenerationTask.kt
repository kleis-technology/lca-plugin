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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
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
    private val logger: TaskLogger,
    private val csvFile: String = "$processName.csv"
) : Task.Backgroundable(project, "Run with ${processName}.csv") {
    companion object {
        private val LOG = Logger.getInstance(ModelGenerationTask::class.java)
    }

    override fun run(indicator: ProgressIndicator) {

        try {
            // read
            indicator.fraction = 0.0
            indicator.text = "Reading $processName.csv"
            val csvFile = Path(containingDirectory.virtualFile.path, csvFile).toFile()
            val requests = csvFile.inputStream().use {
                val requestReader = CsvRequestReader(processName, process.getLabels(), it)
                requestReader.read()
            }

            // process
            runReadAction {
                val collector = LcaFileCollector(file.project)
                val parser = LcaLoader(collector.collect(file), BasicOperations)
                parser.load()
            }

            // write
            val fileName = "$processName.generated.models.lca"
            val outputPath = writeModel(indicator, containingDirectory, requests, fileName)

            // TODO Refactor
            // done
            indicator.text = "Written to $fileName"
            indicator.fraction = 1.0
            val title = "${requests.size} successful assessments of process $processName"
            val message = "Results stored in $fileName"
//            VirtualFileManager.getInstance().refreshAndFindFileByNioPath(outputPath)
//            ApplicationManager.getApplication().runWriteAction{}
//
            ApplicationManager.getApplication().invokeAndWait { ->

//                runWriteAction {
                val vFile = VfsUtil.findFile(
                    outputPath, true
                )
                VirtualFileManager.getInstance().syncRefresh()
                vFile?.refresh(false, false)
                LOG.info("Now refresh")
//                }
                val dumbSrv = DumbService.getInstance(project)
                //ApplicationManager.getApplication().getService(DumbService::class.java)
                dumbSrv.completeJustSubmittedTasks()

            }
            logger.info(title, message)
        } catch (e: EvaluatorException) {
            val title = "Error while assessing $processName"
            logger.info(title, e.message ?: "unknown error")
            LOG.warn("Unable to process computation", e)
        } catch (e: NoSuchElementException) {
            val title = "Error while assessing $processName"
            logger.info(title, e.message ?: "unknown error")
            LOG.warn("Unable to process computation", e)
        } catch (e: FileNotFoundException) {
            val title = "Error while assessing $processName"
            logger.info(title, e.message ?: "unknown error")
            LOG.warn("Unable to process computation, file not found", e)
        }
    }

    private fun writeModel(
        indicator: ProgressIndicator,
        outDir: PsiDirectory,
        results: List<CsvRequest>,
        outFile: String
    ): Path {
        indicator.text = "Writing to $outFile"
        indicator.fraction = 1.0
        val path = Path(outDir.virtualFile.path, outFile)
        OutputToModelWriter(process, results, path).writeModels()
        return path
    }

}