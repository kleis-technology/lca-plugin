package ch.kleis.lcaplugin.language.parser

import com.intellij.psi.tree.IElementType

class LcaTypes {
    companion object {
        fun of(ruleIndex: Int): IElementType {
            return LcaParserDefinition.rules[ruleIndex]
        }
    }
}
