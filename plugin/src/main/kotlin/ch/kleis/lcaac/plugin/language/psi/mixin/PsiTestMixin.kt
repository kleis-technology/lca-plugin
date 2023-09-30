package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.psi.LcaTest
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

abstract class PsiTestMixin(node: ASTNode): ASTWrapperPsiElement(node), LcaTest {
    override fun getName(): String {
        return uid.name
    }

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        for (block in getVariablesList()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }
        return true
    }
}
