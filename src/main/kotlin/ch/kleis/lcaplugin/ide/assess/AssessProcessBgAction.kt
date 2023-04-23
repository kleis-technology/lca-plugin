package ch.kleis.lcaplugin.ide.assess

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.matrix.InventoryError
import ch.kleis.lcaplugin.core.matrix.InventoryResult
import ch.kleis.lcaplugin.language.parser.LcaFileCollector
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.ui.toolwindow.LcaResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory


class AssessProcessBgAction(private val project: Project, private val file: LcaFile, private val processName: String) :
    Task.Backgroundable(project, "Assessing $processName", true) {

    override fun run(progressIndicator: ProgressIndicator) {
        progressIndicator.isIndeterminate = true;

        val parser: LcaLangAbstractParser = runReadAction {
            val collector = LcaFileCollector()
            LcaLangAbstractParser(
                collector.collect(file)
            )
        }
        if (progressIndicator.isCanceled) return;
        val result = try {
            val symbolTable = parser.load()
            if (progressIndicator.isCanceled) return;
            val entryPoint = symbolTable.getTemplate(processName)!!
            if (progressIndicator.isCanceled) return;
            val system = Evaluator(symbolTable).eval(entryPoint)
            if (progressIndicator.isCanceled) return;
            val assessment = Assessment(system)
            if (progressIndicator.isCanceled) return;
            assessment.inventory()
        } catch (e: EvaluatorException) {
            InventoryError(e.message ?: "evaluator: unknown error")
        } catch (e: NoSuchElementException) {
            InventoryError(e.message ?: "evaluator: unknown error")
        } catch (e: IllegalArgumentException) {
            InventoryError("evaluator: system can not be resolved")
        }

        ApplicationManager.getApplication().invokeLater {
            displayToolWindow(project, result)
        }
    }

    private fun displayToolWindow(project: Project, result: InventoryResult) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
        val lcaResult = LcaResult(result)
        val content = ContentFactory.getInstance().createContent(lcaResult.getContent(), project.name, false)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }

}