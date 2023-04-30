package ch.kleis.lcaplugin.language.parser

import com.intellij.psi.tree.IElementType

interface LcaTypes {
    companion object {
        fun rule(ruleIndex: Int): IElementType {
            return LcaParserDefinition.rules[ruleIndex]
        }

        fun token(tokenIndex: Int): IElementType {
            return LcaParserDefinition.tokens[tokenIndex]
        }
    }
}
