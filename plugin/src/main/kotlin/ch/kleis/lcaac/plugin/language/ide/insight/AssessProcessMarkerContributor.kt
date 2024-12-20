package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.actions.AssessProcessAction
import ch.kleis.lcaac.plugin.actions.ContributionAnalysisWithDataAction
import ch.kleis.lcaac.plugin.actions.SensitivityAnalysisAction
import ch.kleis.lcaac.plugin.actions.sankey.SankeyGraphAction
import ch.kleis.lcaac.plugin.language.psi.isProcess
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import ch.kleis.lcaac.plugin.psi.LcaProcess
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement

/*
    https://github.com/JetBrains/intellij-plugins/blob/master/makefile/resources/META-INF/plugin.xml
 */

class AssessProcessMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (isProcess(element)) {
            val process = element.parent as LcaProcess
            val target = process.getProcessRef().getUID().name
            val labels = process.getLabels()
            val assessProcessAction = AssessProcessAction(target, labels)
            val assessProcessWithExternalDataAction = ContributionAnalysisWithDataAction(target, labels)
            val sankeyGraphAction = SankeyGraphAction(target, labels)
            val sensitivityAnalysisAction = SensitivityAnalysisAction(target, labels)
            return Info(
                AllIcons.Actions.Execute,
                {
                    "Run $target"
                },
                assessProcessAction,
                assessProcessWithExternalDataAction,
                sankeyGraphAction,
                sensitivityAnalysisAction,
            )
        }
        return null
    }
}
