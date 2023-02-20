package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.VSystem
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.matrix.InventoryError
import ch.kleis.lcaplugin.core.matrix.InventoryResult
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.ui.toolwindow.LcaResult
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class AssessSystemAction(private val variableName: String) : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val pkgName = file.getPackage().name!!
        val parser = LcaLangAbstractParser()
        val pkg = parser.lcaPackage(pkgName, project)
        val expression = pkg.get(variableName) ?: return
        val evaluator = Evaluator(pkg.getDefinitions())

        try {
            val value = evaluator.eval(expression) as VSystem
            val assessment = Assessment(value)
            val result = assessment.inventory()
            displayToolWindow(project, result)
        } catch (e : EvaluatorException) {
            val result = InventoryError(e.message ?: "evaluator: unknown error")
            displayToolWindow(project, result)
        }
    }

    private fun displayToolWindow(project: Project, result: InventoryResult)  {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
        val lcaResult = LcaResult(result)
        val content = ContentFactory.getInstance().createContent(lcaResult.getContent(), project.name, false)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()

    }
}

