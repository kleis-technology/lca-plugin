package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.actions.RunRunAction
import ch.kleis.lcaac.plugin.language.psi.isRun
import ch.kleis.lcaac.plugin.psi.LcaRun
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement

class RunMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (isRun(element)) {
            val run = element.parent as LcaRun
            val target = run.runRef.name
            val action = RunRunAction(run, target)
            return Info(
                AllIcons.Actions.Execute,
                {
                    "Run $target"
                },
                action,
            )
        }
        return null
    }
}
