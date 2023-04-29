package ch.kleis.lcaplugin.language.psi.stub.techno_product_exchange

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class TechnoProductExchangeStubImpl(
    parent: StubElement<PsiTechnoProductExchange>,
    override val fqn: String,
) : StubBase<PsiTechnoProductExchange>(
    parent,
    LcaElementTypes.TECHNO_PRODUCT_EXCHANGE as IStubElementType<out StubElement<*>, *>
),
    TechnoProductExchangeStub
