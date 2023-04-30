package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiArgument
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

class PsiFromProcessConstraint(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getProcessTemplateRef(): PsiProcessTemplateRef {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_processTemplateRef))?.psi as PsiProcessTemplateRef
    }

    fun getArguments(): Map<String, PsiQuantity> {
        return getPsiArguments()
            .associate { it.name to it.getValue() }
    }

    fun getPsiArguments(): Collection<PsiArgument> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_argument)))
            .map { it.psi as PsiArgument }
    }
}
