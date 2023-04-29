package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.factory.LcaFileFactory
import ch.kleis.lcaplugin.language.psi.factory.LcaUIDFactory
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

class PsiUID(node: ASTNode) : ASTWrapperPsiElement(node), PsiNamedElement {
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
