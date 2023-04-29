package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiUrn(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getParts(): List<String> {
        val first = node.findChildByType(LcaElementTypes.UID)?.psi as PsiUID?
        val localRootName = first?.name ?: throw IllegalStateException()
        val next = node.findChildByType(LcaElementTypes.URN)?.psi as PsiUrn? ?: return listOf(localRootName)
        return listOf(localRootName) + next.getParts()
    }
}
