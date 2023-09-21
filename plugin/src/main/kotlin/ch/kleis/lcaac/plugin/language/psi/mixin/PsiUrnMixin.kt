package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.type.PsiUID
import ch.kleis.lcaac.plugin.language.psi.type.PsiUrn
import ch.kleis.lcaac.plugin.psi.LcaTypes
import ch.kleis.lcaac.plugin.psi.LcaUrn
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUrnMixin(node: ASTNode) : ASTWrapperPsiElement(node), LcaUrn {
    override fun getParts(): List<String> {
        val first = node.findChildByType(LcaTypes.UID)?.psi as PsiUID?
        val localRootName = first?.name ?: throw IllegalStateException()
        val next = node.findChildByType(LcaTypes.URN)?.psi as PsiUrn? ?: return listOf(localRootName)
        return listOf(localRootName) + next.getParts()
    }
}