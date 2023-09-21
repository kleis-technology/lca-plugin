package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.factory.LcaFileFactory
import ch.kleis.lcaac.plugin.language.psi.factory.LcaUIDFactory
import ch.kleis.lcaac.plugin.psi.LcaUid
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract class PsiUIDMixin(node: ASTNode) : ASTWrapperPsiElement(node), LcaUid {
    override fun getName(): String {
        return this.firstChild.text
    }

    override fun setName(name: String): PsiElement {
        val newIdentifier = LcaUIDFactory(
            LcaFileFactory(project)::createFile
        ).createUid(name)
        node.treeParent.replaceChild(node, newIdentifier.node)
        return newIdentifier
    }

    override fun toString(): String {
        return "uid(${this.name})"
    }
}
