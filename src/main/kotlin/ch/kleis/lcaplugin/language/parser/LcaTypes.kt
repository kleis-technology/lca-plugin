package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.grammar.LcaLangParser
import com.intellij.psi.tree.IElementType

interface LcaTypes {
    companion object {
        fun rule(ruleIndex: Int): IElementType {
            return when (ruleIndex) {
                LcaLangParser.RULE_globalAssignment -> LcaStubElementTypes.GLOBAL_ASSIGNMENT
                LcaLangParser.RULE_process -> LcaStubElementTypes.PROCESS
                LcaLangParser.RULE_substance -> LcaStubElementTypes.SUBSTANCE
                LcaLangParser.RULE_technoProductExchange -> LcaStubElementTypes.TECHNO_PRODUCT_EXCHANGE
                LcaLangParser.RULE_unit -> LcaStubElementTypes.UNIT
                else -> LcaParserDefinition.rules[ruleIndex]
            }
        }

        fun token(tokenIndex: Int): IElementType {
            return LcaParserDefinition.tokens[tokenIndex]
        }
    }
}
