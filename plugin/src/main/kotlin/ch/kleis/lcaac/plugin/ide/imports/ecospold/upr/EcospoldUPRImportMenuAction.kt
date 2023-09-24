package ch.kleis.lcaac.plugin.ide.imports.ecospold.upr

import ch.kleis.lcaac.plugin.MyBundle
import ch.kleis.lcaac.plugin.ide.imports.LcaImportDialog
import ch.kleis.lcaac.plugin.ide.imports.ecospold.EcospoldImportSettingsPanel
import ch.kleis.lcaac.plugin.ide.imports.ecospold.settings.UPRSettings
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class EcospoldUPRImportMenuAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val panel = EcospoldImportSettingsPanel(UPRSettings.instance)
        val title = MyBundle.message("lca.dialog.import.ecospold.upr.title")
        val dlg = LcaImportDialog(panel, title, e.project)
        dlg.show()
    }
}
