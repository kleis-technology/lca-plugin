package ch.kleis.lcaac.plugin.ide.imports.ecospold.lcia

import ch.kleis.lcaac.plugin.MyBundle
import ch.kleis.lcaac.plugin.ide.imports.LcaImportDialog
import ch.kleis.lcaac.plugin.ide.imports.ecospold.EcospoldImportSettingsPanel
import ch.kleis.lcaac.plugin.ide.imports.ecospold.settings.LCIASettings
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class EcospoldLCIAImportMenuAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val panel = EcospoldImportSettingsPanel(LCIASettings.instance)
        val title = MyBundle.message("lca.dialog.import.ecospold.lcia.title")
        val dlg = LcaImportDialog(panel, title, e.project)
        dlg.show()
    }
}
