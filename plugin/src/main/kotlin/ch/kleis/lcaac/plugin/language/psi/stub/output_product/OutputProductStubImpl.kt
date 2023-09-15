package ch.kleis.lcaac.plugin.language.psi.stub.output_product

import ch.kleis.lcaac.plugin.psi.LcaOutputProductSpec
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class OutputProductStubImpl(
    parent: StubElement<LcaOutputProductSpec>,
    override val fqn: String,
) : StubBase<LcaOutputProductSpec>(
    parent,
    LcaTypes.OUTPUT_PRODUCT_SPEC as IStubElementType<out StubElement<*>, *>
), OutputProductStub
