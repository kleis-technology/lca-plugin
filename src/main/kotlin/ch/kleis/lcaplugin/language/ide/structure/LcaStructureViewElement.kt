package ch.kleis.lcaplugin.language.ide.structure

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.psi.LcaProcess
import ch.kleis.lcaplugin.psi.impl.LcaProcessImpl
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.util.PsiTreeUtil


class LcaStructureViewElement(private val element: NavigatablePsiElement)
    : StructureViewTreeElement, SortableTreeElement {


    override fun getValue(): Any {
        return element
    }

    override fun navigate(requestFocus: Boolean) {
        element.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean {
        return element.canNavigate()
    }

    override fun canNavigateToSource(): Boolean {
        return element.canNavigateToSource()
    }

    override fun getAlphaSortKey(): String {
        return element.name ?: ""
    }

    override fun getPresentation(): ItemPresentation {
        return element.presentation ?: PresentationData(element.name,
            null, AllIcons.Nodes.Class, null);
    }

    override fun getChildren(): Array<TreeElement> {
        if (element is LcaFile) {
            val definitions: List<LcaProcess> = PsiTreeUtil.getChildrenOfTypeAsList(
                element,
                LcaProcess::class.java
            )
            val treeElements: MutableList<TreeElement> = ArrayList(definitions.size)
            for (def in definitions) {
                treeElements.add(LcaStructureViewElement(def as LcaProcessImpl))
            }
            return treeElements.toTypedArray()
        }
        return emptyArray()
    }

}
