package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis

import ch.kleis.lcaac.plugin.MyBundle
import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ide.component.ComponentFactory
import ch.kleis.lcaac.plugin.ide.component.ComponentFactory.Companion.createLocationComponent
import ch.kleis.lcaac.plugin.ui.toolwindow.LcaToolWindowContent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import org.apache.commons.csv.CSVFormat
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JButton
import javax.swing.JPanel

// TODO: Remove this
class ContributionAnalysisHugeWindow(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    observablePortComparator: Comparator<MatrixColumnIndex<BasicNumber>>,
    messageKey: String,
    val project: Project,
) : LcaToolWindowContent {
    private val sortedObservablePorts = analysis.getObservablePorts().getElements().sortedWith(observablePortComparator)
    private val sortedControllablePorts = analysis.getControllablePorts().getElements().sortedBy { it.getUID() }

    companion object {
        private val LOG = Logger.getInstance(ContributionAnalysisHugeWindow::class.java)
    }

    private val content: JPanel
    private val settings = SaveSettings.instance

    init {
        val builder = FormBuilder()
        val label = JBLabel(MyBundle.message(messageKey))
        builder.addComponent(label)
        val locComp = createLocationComponent(
            { settings.saveFolder },
            { s: String -> settings.saveFolder = s }
        )
        builder.addLabeledComponent(locComp.label, locComp.component)
        val fileComp = ComponentFactory.createTextComponent(
            "lca.dialog.export.filename.label",
            { settings.fileName },
            { s: String -> settings.fileName = s }
        )
        builder.addLabeledComponent(fileComp.label, fileComp.component)
        val button = JButton("Save as CSV file")
        button.addActionListener { save() }
        builder.addComponent(button)

        content = builder.panel
        content.border = JBUI.Borders.empty(0, 20)
        content.updateUI()
    }

    private fun save() {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Saving your data") {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                try {
                    val start = System.currentTimeMillis()
                    val path = Paths.get(settings.saveFolder + File.separatorChar + settings.fileName)
                    Files.createDirectories(path.parent)
                    val out = FileWriter(path.toFile())
                    out.use {

                        val builder = CSVFormat.Builder.create().setHeader(*getHeaders())
                        val printer = builder.build().print(out)
                        sortedObservablePorts.forEach {
                            printer.printRecord(*getRow(it))
                        }
                    }
                    val duration = (System.currentTimeMillis() - start) / 1000
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("LcaAsCode")
                        .createNotification(
                            MyBundle.message(
                                "lca.dialog.export.finished.success",
                                duration,
                                path
                            ), NotificationType.INFORMATION
                        )
                        .notify(project)
                    VirtualFileManager.getInstance().refreshAndFindFileByNioPath(path)
                } catch (e: Exception) {
                    val title = "Error while saving results to file"
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("LcaAsCode")
                        .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                        .notify(project)
                    LOG.warn("Unable to process computation", e)
                }
            }
        })
    }

    private fun getHeaders(): Array<String> {
        val cols = sortedControllablePorts
            .map { "${it.getDisplayName()} [${it.referenceUnit().symbol}]" }
        return (listOf("Product", "Quantity", "Unit") + cols).toTypedArray()
    }

    private fun getRow(outputProduct: MatrixColumnIndex<BasicNumber>): Array<String> {
        val cells = sortedControllablePorts
            .map { analysis.getPortContribution(outputProduct, it).amount.toString() }

        val quantity = analysis.supplyOf(outputProduct)
        return (listOf(
            outputProduct.getDisplayName(),
            "${quantity.amount}",
            "${quantity.unit.symbol}"
        ) + cells).toTypedArray()
    }

    override fun getContent(): JPanel {
        return content
    }
}
