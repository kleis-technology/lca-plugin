package ch.kleis.lcaac.plugin.ide.template

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

        private fun containsAllErrors(elt: PsiErrorElement, vararg strings: String): Boolean {
            return strings.all { elt.errorDescription.contains(it) }
        }
    }

}
