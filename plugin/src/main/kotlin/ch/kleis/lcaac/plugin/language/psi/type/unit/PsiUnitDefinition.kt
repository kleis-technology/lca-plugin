package ch.kleis.lcaac.plugin.language.psi.type.unit

import ch.kleis.lcaac.plugin.language.psi.stub.unit.UnitStub
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

enum class UnitDefinitionType {
    LITERAL, ALIAS
}

interface PsiUnitDefinition : PsiNameIdentifierOwner, StubBasedPsiElement<UnitStub> {
    override fun getName(): String
    fun getType(): UnitDefinitionType
}
