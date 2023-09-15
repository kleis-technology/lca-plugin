package ch.kleis.lcaac.plugin.language.psi.stub.unit

import ch.kleis.lcaac.plugin.psi.LcaTypes
import ch.kleis.lcaac.plugin.psi.LcaUnitDefinition
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class UnitStubImpl(
    parent: StubElement<LcaUnitDefinition>,
    override val fqn: String,
) : StubBase<LcaUnitDefinition>(
    parent,
    LcaTypes.UNIT_DEFINITION as IStubElementType<out StubElement<*>, *>
), UnitStub
