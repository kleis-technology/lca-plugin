package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.psi.LcaProcessRef
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class LcaProcessRefAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LcaProcessRef) {
            return
        }

//        val targets =
//            (element.reference.resolve()
//        val products = element.getUID()
//        if (products.size <= 1) {
//            return
//        }

//        val productNames = products.map { it.outputProductSpec.name }
//        val productsWithoutAllocationFactors = products
//            .filter { it.outputProductSpec.allocateField == null }
//        if (productsWithoutAllocationFactors.isNotEmpty()) {
//            annotateErrWithMessage(
//                element.blockProductsList.first(),
//                holder,
//                "some products in $productNames are missing allocation factors",
//            )
//        }
    }
}
