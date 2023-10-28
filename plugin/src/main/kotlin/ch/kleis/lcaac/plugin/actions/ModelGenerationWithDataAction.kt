package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.plugin.actions.tasks.EventTaskLogger
import ch.kleis.lcaac.plugin.actions.tasks.ModelGenerationTask
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.LcaProcess
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.progress.ProgressManager

class ModelGenerationWithDataAction(
    private val processName: String,
    private val process: LcaProcess,
) : AnAction(
    AllIcons.Actions.Execute,
) {
    init {
        this.templatePresentation.setText("Generate LCA Model with ${process.name}.csv", false)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val containingDirectory = file.containingDirectory ?: return

        val task = ModelGenerationTask(
            project, process, processName, file,
            containingDirectory,
            logger = EventTaskLogger(project)
        )
        ProgressManager.getInstance().run(task)
    }

}
