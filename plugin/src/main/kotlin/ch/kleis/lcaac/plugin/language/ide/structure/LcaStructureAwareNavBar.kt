package ch.kleis.lcaac.plugin.language.ide.structure

import ch.kleis.lcaac.plugin.LcaLanguage
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.LcaBlockVariables
import ch.kleis.lcaac.plugin.psi.LcaProcess
import ch.kleis.lcaac.plugin.psi.LcaSubstance
import ch.kleis.lcaac.plugin.psi.LcaUnitDefinition
import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.lang.Language

class LcaStructureAwareNavBar : StructureAwareNavBarModelExtension() {
    override val language: Language
        get() = LcaLanguage.INSTANCE

    override fun getPresentableText(type: Any?): String? {
        return when(type) {
            is LcaFile -> type.name
            is LcaProcess -> type.name
            is LcaSubstance -> type.name
            is LcaUnitDefinition -> type.name
            is LcaBlockVariables -> "variables"
            else -> null
        }
    }
}
