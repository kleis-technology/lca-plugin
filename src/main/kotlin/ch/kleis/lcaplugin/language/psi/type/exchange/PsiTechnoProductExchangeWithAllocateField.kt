package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.field.PsiAllocateField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiTechnoProductExchangeWithAllocateField(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getTechnoProductExchange(): PsiTechnoProductExchange {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_technoProductExchange))?.psi as PsiTechnoProductExchange
    }
    fun getAllocateField(): PsiAllocateField {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_allocateField))?.psi as PsiAllocateField
    }
}
