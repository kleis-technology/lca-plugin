package ch.kleis.lcaac.plugin.language.loader

import ch.kleis.lcaac.plugin.language.LcaLexer
import com.intellij.lexer.FlexAdapter

class LcaLexerAdapter : FlexAdapter(LcaLexer(null))
