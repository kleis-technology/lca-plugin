package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.plugin.actions.tasks.SensitivityAnalysisTask
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.progress.ProgressManager

class SensitivityAnalysisAction(
    private val processName: String,
    private val matchLabels: Map<String, String>,
) : AnAction(
    "Analyze Sensitivity",
    "Analyze sensitivity",
    AllIcons.Actions.Execute,
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        ProgressManager.getInstance().run(SensitivityAnalysisTask(
            project, file, processName, matchLabels
        ))
    }
}
