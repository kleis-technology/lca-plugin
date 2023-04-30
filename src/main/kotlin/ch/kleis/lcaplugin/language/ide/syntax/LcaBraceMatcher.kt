package ch.kleis.lcaplugin.language.ide.syntax

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.language.parser.LcaTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class LcaBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> {

        return arrayOf(
            BracePair(LcaTypes.token(LcaLangLexer.LBRACE), LcaTypes.token(LcaLangLexer.RBRACE), true),
            BracePair(LcaTypes.token(LcaLangLexer.LPAREN), LcaTypes.token(LcaLangLexer.RPAREN), true),
        )
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true
    }

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }
}
