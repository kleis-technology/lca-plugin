package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

enum class UnitPrimitiveType {
    DEFINITION, PAREN, VARIABLE
}

class PsiUnitPrimitive(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getType(): UnitPrimitiveType {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_unitDefinition))?.let { UnitPrimitiveType.DEFINITION }
            ?: node.findChildByType(LcaTypes.token(LcaLangLexer.LPAREN))?.let { UnitPrimitiveType.PAREN }
            ?: UnitPrimitiveType.VARIABLE
    }

    fun getDefinition(): PsiUnitDefinition {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_unitDefinition))?.psi as PsiUnitDefinition
    }

    fun getUnitInParen(): PsiUnit {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_unit))?.psi as PsiUnit
    }

    fun getRef(): PsiUnitRef {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_unitRef))?.psi as PsiUnitRef
    }
}
