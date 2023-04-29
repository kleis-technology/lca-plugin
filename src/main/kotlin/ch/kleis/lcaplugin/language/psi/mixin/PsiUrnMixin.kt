package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiUID
import ch.kleis.lcaplugin.language.psi.type.PsiUrn
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUrnMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUrn {
    override fun getParts(): List<String> {
        val first = node.findChildByType(LcaElementTypes.UID)?.psi as PsiUID?
        val localRootName = first?.name ?: throw IllegalStateException()
        val next = node.findChildByType(LcaElementTypes.URN)?.psi as PsiUrn? ?: return listOf(localRootName)
        return listOf(localRootName) + next.getParts()
    }
}
