package ch.kleis.lcaac.plugin.psi

import ch.kleis.lcaac.plugin.LcaLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class LcaElementType(debugName: @NonNls String) : IElementType(debugName, LcaLanguage.INSTANCE)
