package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.LcaLexer
import com.intellij.lexer.FlexAdapter

class LcaLexerAdapter : FlexAdapter(ch.kleis.lcaplugin.language.LcaLexer(null)) {
}
