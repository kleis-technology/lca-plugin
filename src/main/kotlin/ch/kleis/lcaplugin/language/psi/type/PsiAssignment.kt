package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

class PsiAssignment(node: ASTNode) : ASTWrapperPsiElement(node), PsiNameIdentifierOwner {
    fun getQuantityRef(): PsiQuantityRef {
        return node.findChildByType(LcaElementTypes.QUANTITY_REF)?.psi as PsiQuantityRef
    }

    fun getValue(): PsiQuantity {
        return node.findChildByType(LcaElementTypes.QUANTITY)?.psi as PsiQuantity
    }

    override fun getName(): String {
        return getQuantityRef().name
    }

    override fun setName(name: String): PsiElement {
        getQuantityRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getQuantityRef().nameIdentifier
    }
}
