package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

class PsiArgument(node: ASTNode) : ASTWrapperPsiElement(node), PsiNameIdentifierOwner {
    fun getParameterRef(): PsiParameterRef {
        return node.findChildByType(LcaElementTypes.PARAMETER_REF)?.psi as PsiParameterRef
    }

    fun getValue(): PsiQuantity {
        return node.findChildByType(LcaElementTypes.QUANTITY)?.psi as PsiQuantity
    }

    override fun getName(): String {
        return getParameterRef().name
    }

    override fun setName(name: String): PsiElement {
        getParameterRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getParameterRef().nameIdentifier
    }
}
