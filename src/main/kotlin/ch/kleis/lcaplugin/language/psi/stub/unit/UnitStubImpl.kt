package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class UnitStubImpl(
    parent: StubElement<PsiUnitDefinition>,
    override val fqn: String,
) : StubBase<PsiUnitDefinition>(
    parent,
    LcaElementTypes.UNIT_DEFINITION as IStubElementType<out StubElement<*>, *>
), UnitStub
