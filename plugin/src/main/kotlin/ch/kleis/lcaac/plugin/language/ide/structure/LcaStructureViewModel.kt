package ch.kleis.lcaac.plugin.language.ide.structure

import ch.kleis.lcaac.plugin.psi.LcaBlockVariables
import ch.kleis.lcaac.plugin.psi.LcaProcess
import ch.kleis.lcaac.plugin.psi.LcaSubstance
import ch.kleis.lcaac.plugin.psi.LcaUnitDefinition
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class LcaStructureViewModel(editor: Editor?, psiFile: PsiFile) :
    StructureViewModelBase(psiFile, editor, LcaStructureViewElement(psiFile)), StructureViewModel.ElementInfoProvider {


    override fun getSorters(): Array<Sorter> {
        return arrayOf(Sorter.ALPHA_SORTER)
    }

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement?): Boolean {
        return false
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        return element is LcaProcess
            || element is LcaSubstance
            || element is LcaUnitDefinition
            || element is LcaBlockVariables
    }

    override fun getSuitableClasses(): Array<Class<*>> {
        return arrayOf(
            LcaProcess::class.java,
            LcaSubstance::class.java,
            LcaUnitDefinition::class.java,
            LcaBlockVariables::class.java,
        )
    }
}

