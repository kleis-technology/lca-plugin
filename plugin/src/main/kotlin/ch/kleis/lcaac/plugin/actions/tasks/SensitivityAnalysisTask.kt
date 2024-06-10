package ch.kleis.lcaac.plugin.actions.tasks

import arrow.core.filterIsInstance
import ch.kleis.lcaac.core.ParameterName
import ch.kleis.lcaac.core.assessment.SensitivityAnalysis
import ch.kleis.lcaac.core.assessment.SensitivityAnalysisProgram
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.dual.DualNumber
import ch.kleis.lcaac.core.math.dual.DualOperations
import ch.kleis.lcaac.core.matrix.IndexedCollection
import ch.kleis.lcaac.core.matrix.ParameterVector
import ch.kleis.lcaac.plugin.language.loader.LcaFileCollector
import ch.kleis.lcaac.plugin.language.loader.LcaLoader
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaac.plugin.psi.LcaStringExpression
import ch.kleis.lcaac.plugin.ui.toolwindow.sensitivity_analysis.SensitivityAnalysisWindow
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class SensitivityAnalysisTask(
    project: Project,
    private val file: LcaFile,
    private val processName: String,
    private val matchLabels: Map<String, String>,
) : Task.Backgroundable(project, "Sensitivity analysis") {
    private var analysis: SensitivityAnalysis? = null

    companion object {
        private val LOG = Logger.getInstance(SensitivityAnalysisTask::class.java)
    }

    fun getAnalysis(): SensitivityAnalysis? {
        return analysis
    }

    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = true

        // nb params via psi
        val nbQuantitativeParams = runReadAction {
            val fqn = "${file.getPackageName()}.$processName"
            ProcessStubKeyIndex.findProcesses(project, fqn, matchLabels).first().getParameters()
                .filter { it.value !is LcaStringExpression }
                .size
        }
        val ops = DualOperations(nbQuantitativeParams)
        if (nbQuantitativeParams == 0) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("LcaAsCode")
                .createNotification(
                    "No quantitative parameters found.",
                    "Sensitivity analysis requires at least one quantitative parameter.",
                    NotificationType.WARNING,
                )
                .notify(project)
            return
        }

        // read
        indicator.text = "Loading symbol table"
        val symbolTable = runReadAction {
            val collector = LcaFileCollector(file.project)
            val parser = LcaLoader(collector.collect(file), ops)
            parser.load()
        }

        // compute
        indicator.text = "Solving system"
        val template =
            symbolTable.getTemplate(
                processName,
                matchLabels
            )!! // We are called from a process, so it must exist
        val sourceOps = DefaultDataSourceOperations(
            LcaacConfig(),
            ops,
            project.basePath!!,
        )
        val (arguments, parameters) =
            prepareArguments(ops, sourceOps, symbolTable, template.params)
        val trace = Evaluator(symbolTable, ops, sourceOps).trace(template, arguments)
        this.analysis = SensitivityAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint(), parameters).run()
    }

    override fun onSuccess() {
        this.analysis?.let {
            displayAnalysis(project, it)
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

    private fun displayAnalysis(
        project: Project,
        analysis: SensitivityAnalysis,
    ) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
        val assessResultContent = SensitivityAnalysisWindow(analysis, project, processName).getContent()
        val content = ContentFactory.getInstance().createContent(
            assessResultContent,
            "Sensitivity analysis of $processName",
            false,
        )
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }

    private fun prepareArguments(
        ops: DualOperations,
        sourceOps: DataSourceOperations<DualNumber>,
        symbolTable: SymbolTable<DualNumber>,
        params: Map<String, DataExpression<DualNumber>>
    ): Pair<Map<String, DataExpression<DualNumber>>, ParameterVector<DualNumber>> {
        val dataReducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, sourceOps)
        val reduced = params.mapValues { dataReducer.reduce(it.value) }
        val quantitativeArgumentList = reduced.filterIsInstance<String, EQuantityScale<DualNumber>>()
            .toList()
            .mapIndexed { index: Int, (name, value): Pair<String, EQuantityScale<DualNumber>> ->
                with(ops) {
                    name to EQuantityScale(
                        value.scale * (pure(1.0) + basis(index)),
                        value.base,
                    )
                }
            }
        val parameters = ParameterVector(
            names = IndexedCollection(quantitativeArgumentList.map { ParameterName(it.first) }),
            data = with(ToValue(ops)) { quantitativeArgumentList.map { it.second.toValue() as QuantityValue<DualNumber> } }
        )
        val arguments = reduced.plus(quantitativeArgumentList)
        return arguments to parameters
    }
}
