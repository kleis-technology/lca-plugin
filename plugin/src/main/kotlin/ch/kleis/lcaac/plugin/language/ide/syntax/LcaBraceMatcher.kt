package ch.kleis.lcaac.plugin.language.ide.syntax

import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class LcaBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> {
        return arrayOf(
            BracePair(LcaTypes.LBRACE, LcaTypes.RBRACE, true),
            BracePair(LcaTypes.LSQBRACE, LcaTypes.RSQBRACE, true),
            BracePair(LcaTypes.LPAREN, LcaTypes.RPAREN, true),
            BracePair(LcaTypes.DOUBLE_QUOTE, LcaTypes.DOUBLE_QUOTE, true)
        )
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true
    }

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }
}
