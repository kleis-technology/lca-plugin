package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnit
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiUnitField(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getValue(): PsiUnit {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_unit))?.psi as PsiUnit
    }
}
