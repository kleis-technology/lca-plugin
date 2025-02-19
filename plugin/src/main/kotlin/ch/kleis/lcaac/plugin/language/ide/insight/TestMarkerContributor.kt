package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.actions.RunTestAction
import ch.kleis.lcaac.plugin.language.psi.isTest
import ch.kleis.lcaac.plugin.psi.LcaTest
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement

class TestMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (isTest(element)) {
            val test = element.parent as LcaTest
            val target = test.testRef.name
            val action = RunTestAction(target)
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
