package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.type.PsiBlockForEach
import ch.kleis.lcaac.plugin.psi.*
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil

abstract class PsiBlockForEachMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlockForEach {
    override fun getDataRef(): LcaDataRef {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaDataRef::class.java).elementAt(0)
    }

    override fun getValue(): LcaDataSourceExpression {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaDataSourceExpression::class.java).elementAt(0)
    }

    override fun getName(): String {
        return getDataRef().name
    }

    override fun setName(name: String): PsiElement {
        getDataRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getDataRef().nameIdentifier
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
        return processor.execute(this, state)
    }

    override fun getVariablesList(): Collection<LcaVariables> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaVariables::class.java)
    }
}
