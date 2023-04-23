package ch.kleis.lcaplugin.ide.assess

import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.progress.ProgressManager

class AssessProcessAction(private val processName: String) : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val lcaFile = e.getData(LangDataKeys.PSI_FILE) as LcaFile?
        lcaFile?.let { file: LcaFile ->
            e.project?.let {
                ProgressManager.getInstance().run(AssessProcessBgAction(it, file, processName))
            }
        }
    }

}


