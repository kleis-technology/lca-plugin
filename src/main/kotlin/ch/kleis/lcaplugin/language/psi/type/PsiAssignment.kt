package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

class PsiAssignment(node: ASTNode) : ASTWrapperPsiElement(node), PsiNameIdentifierOwner {
    fun getQuantityRef(): PsiQuantityRef {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantityRef))?.psi as PsiQuantityRef
    }

    fun getValue(): PsiQuantity {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantity))?.psi as PsiQuantity
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
