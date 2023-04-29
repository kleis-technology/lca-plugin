package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.grammar.LcaLangLexer
import com.intellij.psi.tree.TokenSet
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory

class LcaLangTokenSets {
    companion object {
        val COMMENTS : TokenSet = PSIElementTypeFactory.createTokenSet(
            LcaLanguage.INSTANCE,
            LcaLangLexer.COMMENT,
            LcaLangLexer.LINE_COMMENT,
        )
        val STRING_LITERALS : TokenSet = PSIElementTypeFactory.createTokenSet(
            LcaLanguage.INSTANCE,
            LcaLangLexer.STRING_LITERAL,
        )
        val ID : TokenSet = PSIElementTypeFactory.createTokenSet(
            LcaLanguage.INSTANCE,
            LcaLangLexer.ID,
        )
        fun create(vararg types: Int): TokenSet {
            return PSIElementTypeFactory.createTokenSet(
                LcaLanguage.INSTANCE,
                *types
            )
        }
    }
}
