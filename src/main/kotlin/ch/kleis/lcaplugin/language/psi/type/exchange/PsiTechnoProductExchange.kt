package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiTechnoProductExchange : PsiNameIdentifierOwner {
    fun getProductRef(): PsiProductRef {
        return node.findChildByType(LcaElementTypes.PRODUCT_REF)?.psi as PsiProductRef
    }

    fun getQuantity(): PsiQuantity {
        return node.findChildByType(LcaElementTypes.QUANTITY)?.psi as PsiQuantity
    }

    override fun getName(): String {
        return getProductRef().name
    }

    override fun setName(name: String): PsiElement {
        getProductRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getProductRef().nameIdentifier
    }
}
