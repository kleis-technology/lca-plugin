package ch.kleis.lcaplugin.language.ide.syntax

import ch.kleis.lcaplugin.psi.LcaElementTypes
import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class LcaBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> {
        return arrayOf(
            BracePair(LcaTokenTypes.LBRACE, LcaTokenTypes.RBRACE, true),
            BracePair(LcaTokenTypes.LSQBRACE, LcaTokenTypes.RSQBRACE, true),
            BracePair(LcaTokenTypes.LPAREN, LcaTokenTypes.RPAREN, true),
        )
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true
    }

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }
}
