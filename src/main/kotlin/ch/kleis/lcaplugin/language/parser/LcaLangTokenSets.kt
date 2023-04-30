package ch.kleis.lcaplugin.language.parser

import com.intellij.psi.tree.TokenSet

class LcaLangTokenSets {
    companion object {
        fun create(ruleIndex: Int): TokenSet {
            return TokenSet.create(LcaParserDefinition.rules[ruleIndex])
        }
    }
}
