package ch.kleis.lcaac.plugin.psi

import ch.kleis.lcaac.plugin.LcaLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class LcaTokenType(debugName: @NonNls String) : IElementType(debugName, LcaLanguage.INSTANCE) {
    override fun toString(): String {
        return "LcaTokenType." + super.toString()
    }
}
