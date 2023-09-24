package ch.kleis.lcaac.plugin.language.ide.structure

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.impl.LcaGlobalVariablesImpl
import ch.kleis.lcaac.plugin.psi.impl.LcaProcessImpl
import ch.kleis.lcaac.plugin.psi.impl.LcaSubstanceImpl
import ch.kleis.lcaac.plugin.psi.impl.LcaUnitDefinitionImpl
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement

class LcaStructureViewElement(
    private val element: NavigatablePsiElement
) : StructureViewTreeElement, SortableTreeElement {
    override fun getPresentation(): ItemPresentation {
        // TODO: Design LCA specific icons
        return element.presentation ?: when (element) {
            is LcaProcessImpl -> PresentationData(element.name, null, AllIcons.Nodes.Property, null)
            is LcaSubstanceImpl -> PresentationData(element.name, null, AllIcons.Nodes.Static, null)
            is LcaUnitDefinitionImpl -> PresentationData(element.name, null, AllIcons.Nodes.Variable, null)
            is LcaGlobalVariablesImpl -> PresentationData("variables", null, AllIcons.Nodes.Gvariable, null)
            else -> PresentationData(element.name, null, AllIcons.Nodes.Unknown, null)
        }
    }

    override fun getChildren(): Array<TreeElement> {
        return when (element) {
            is LcaFile -> {
                val processes: Collection<NavigatablePsiElement> =
                    element.getProcesses().filterIsInstance<LcaProcessImpl>()
                val substances: Collection<NavigatablePsiElement> =
                    element.getSubstances().filterIsInstance<LcaSubstanceImpl>()
                val units: Collection<NavigatablePsiElement> =
                    element.getUnitDefinitions().filterIsInstance<LcaUnitDefinitionImpl>()
                val variables: Collection<NavigatablePsiElement> =
                    element.getBlocksOfGlobalVariables().filterIsInstance<LcaGlobalVariablesImpl>()
                val all = processes
                    .plus(substances)
                    .plus(units)
                    .plus(variables)
                    .map { LcaStructureViewElement(it) }
                return all.toTypedArray()
            }

            else -> emptyArray<TreeElement>()
        }
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

    override fun getValue(): Any {
        return element
    }

    override fun getAlphaSortKey(): String {
        return element.name ?: ""
    }
}
