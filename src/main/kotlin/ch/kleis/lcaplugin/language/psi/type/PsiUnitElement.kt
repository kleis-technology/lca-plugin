package ch.kleis.lcaplugin.language.psi.type

import com.intellij.psi.PsiNamedElement
import javax.measure.Unit

interface PsiUnitElement : PsiNamedElement {

    fun getUnit() : Unit<*>
}
