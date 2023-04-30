package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import com.intellij.psi.PsiElement

interface PsiExchange: PsiElement {
    fun getQuantity(): PsiQuantity {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantity))?.psi as PsiQuantity
    }
}
