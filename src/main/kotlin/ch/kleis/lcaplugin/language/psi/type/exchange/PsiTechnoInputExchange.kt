package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.PsiFromProcessConstraint
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class PsiTechnoInputExchange(node: ASTNode) : ASTWrapperPsiElement(node), PsiExchange {
    fun getProductRef(): PsiProductRef {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_productRef))?.psi as PsiProductRef
    }

    fun getFromProcessConstraint(): PsiFromProcessConstraint? {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_fromProcessConstraint))?.psi as PsiFromProcessConstraint?
    }
}
