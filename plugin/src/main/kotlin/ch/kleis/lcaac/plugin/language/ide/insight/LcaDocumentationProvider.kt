package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.ide.insight.LcaDocumentGenerator.generateAssignment
import ch.kleis.lcaac.plugin.language.ide.insight.LcaDocumentGenerator.generateGlobalAssignment
import ch.kleis.lcaac.plugin.language.ide.insight.LcaDocumentGenerator.generateProcess
import ch.kleis.lcaac.plugin.language.ide.insight.LcaDocumentGenerator.generateProduct
import ch.kleis.lcaac.plugin.language.ide.insight.LcaDocumentGenerator.generateSubstance
import ch.kleis.lcaac.plugin.language.ide.insight.LcaDocumentGenerator.generateUnitDefinition
import ch.kleis.lcaac.plugin.psi.*
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement

/** The API is deprecated, but the new one is still experimental in June 2023 (see
 *  [com.intellij.platform.backend.documentation.DocumentationTarget] and
 * [com.intellij.platform.backend.presentation.TargetPresentation])
 */
class LcaDocumentationProvider : AbstractDocumentationProvider() {
    private companion object {
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return super.getQuickNavigateInfo(element, originalElement)
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return when (element) {
            is LcaSubstance -> generateSubstance(element)

            is LcaOutputProductSpec -> generateProduct(element)

            is LcaProcess -> generateProcess(element)

            is LcaUnitDefinition -> generateUnitDefinition(element)

            is LcaGlobalAssignment -> generateGlobalAssignment(element)

            is LcaAssignment -> generateAssignment(element)

            is LcaParameterRef -> {
                val target = element.reference.resolve()
                if (target != null && target is LcaAssignment) generateAssignment(target)
                else super.generateDoc(element, originalElement)
            }

            is LcaUid -> {
                val parent = element.parent.reference?.resolve()
                if (parent != null && parent is LcaProcess) generateProcess(parent)
                else super.generateDoc(element, originalElement)
            }

            else -> super.generateDoc(element, originalElement)
        }
    }
}
