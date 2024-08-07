package ch.kleis.lcaac.plugin.language.type_checker

import ch.kleis.lcaac.plugin.psi.*
import com.intellij.psi.PsiElement

class LcaMatchLabelsEvaluator {
    private val rec = RecursiveGuard()

    fun evalOrNull(labels: LcaMatchLabels): Map<String, String>? {
        return try {
            eval(labels)
        } catch (e: PsiTypeCheckException) {
            null
        }
    }

    fun eval(labels: LcaMatchLabels): Map<String, String> {
        return labels.labelSelectorList
            .associate { selector ->
                selector.dataExpression?.let {
                    selector.labelRef.name to evalDataExpression(it)
                } ?: return emptyMap()
            }
    }

    private fun evalDataExpression(element: PsiElement): String {
        return when (element) {
            is LcaStringExpression -> element.text.trim('"')
            is LcaDataRef -> evalDataRef(element)
            is LcaAssignment -> evalDataExpression(element.getValue())
            is LcaGlobalAssignment -> evalDataExpression(element.getValue())
            is LcaLabelAssignment -> element.getValue()
            else -> throw PsiTypeCheckException("${element.text} is not a valid label")
        }
    }

    private fun evalDataRef(element: LcaDataRef): String {
        return rec.guard { el: LcaDataRef ->
            el.reference.resolve()
                ?.let { evalDataExpression(it) }
                ?: throw PsiTypeCheckException("unresolved reference ${element.text}")
        }(element)
    }
}
