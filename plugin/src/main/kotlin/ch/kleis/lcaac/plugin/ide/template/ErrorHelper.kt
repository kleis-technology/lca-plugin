package ch.kleis.lcaac.plugin.ide.template

import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement

class ErrorHelper {
    companion object {
        fun isInErrorInRootBlock(element: PsiElement?): Boolean {
            val parent = element?.parent
            return parent != null &&
                    parent is PsiErrorElement &&
                    containsAllErrors(parent, "process", "substance")
        }

        fun containsAllErrors(elt: PsiErrorElement, vararg strings: String): Boolean {
            return strings.all { elt.errorDescription.contains("LcaTokenType.${it}") }
        }
    }

}
