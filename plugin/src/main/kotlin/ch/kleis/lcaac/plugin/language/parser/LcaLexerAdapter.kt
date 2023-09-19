package ch.kleis.lcaac.plugin.language.parser

import ch.kleis.lcaac.plugin.language.LcaLexer
import com.intellij.lexer.FlexAdapter

class LcaLexerAdapter : FlexAdapter(LcaLexer(null))
