package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.language.psi.stub.global_assignment.GlobalAssignmentStub
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaac.plugin.psi.LcaDataExpression
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiGlobalAssignment : StubBasedPsiElement<GlobalAssignmentStub>, PsiNameIdentifierOwner {
    override fun getName(): String

    fun getDataRef(): PsiDataRef

    fun getValue(): LcaDataExpression
}
