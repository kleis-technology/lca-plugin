package ch.kleis.lcaac.plugin.ui.toolwindow.shared

import ch.kleis.lcaac.plugin.MyBundle
import ch.kleis.lcaac.plugin.actions.tasks.TaskLogger
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import org.apache.commons.csv.CSVFormat
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.table.TableModel


class SaveTableModelTask(
    private val project: Project,
    private val model: TableModel,
    private val file: File,
    private val logger: TaskLogger
) : Task.Backgroundable(project, "Saving data") {
    companion object {
        private val LOG = Logger.getInstance(SaveTableModelTask::class.java)
    }

    override fun run(indicator: ProgressIndicator) {
        try {
            val start = System.currentTimeMillis()
            val path = Paths.get(file.path)
            Files.createDirectories(path.parent)
            val out = FileWriter(path.toFile())
            out.use {
                val builder = CSVFormat.Builder.create().setHeader(*getHeaders(model))
                val printer = builder.build().print(out)
                getRows(model).forEach {
                    printer.printRecord(*it)
                }
            }
            val duration = (System.currentTimeMillis() - start) / 1000
            logger.info(
                "Save Model", MyBundle.message(
                    "lca.dialog.export.finished.success",
                    duration,
                    path
                )
            )
            VirtualFileManager.getInstance().refreshAndFindFileByNioPath(path)
        } catch (e: Exception) {
            val title = "Error while saving results to file"
            logger.error(title, e.message ?: "unknown error")
            LOG.warn("Unable to process computation", e)
        }
    }

    private fun getRows(table: TableModel): List<Array<String>> {
        return IntRange(0, table.rowCount - 1).map { rowIndex ->
            IntRange(0, table.columnCount - 1).map { columnIndex ->
                "${model.getValueAt(rowIndex, columnIndex)}"
            }.toTypedArray()
        }
    }

    private fun getHeaders(table: TableModel): Array<String> {
        return IntRange(0, table.columnCount - 1).map {
            model.getColumnName(it)
        }.toTypedArray()
    }
}
