package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiAllocateField(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getValue(): PsiQuantity {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantity))?.psi as PsiQuantity
    }
}
