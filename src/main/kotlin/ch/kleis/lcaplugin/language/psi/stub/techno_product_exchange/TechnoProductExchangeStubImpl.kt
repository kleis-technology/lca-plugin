package ch.kleis.lcaplugin.language.psi.stub.techno_product_exchange

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class TechnoProductExchangeStubImpl(
    parent: StubElement<PsiTechnoProductExchange>,
    override val fqn: String,
) : StubBase<PsiTechnoProductExchange>(
    parent,
    LcaTypes.rule(LcaLangParser.RULE_technoProductExchange) as IStubElementType<out StubElement<*>, *>
),
    TechnoProductExchangeStub
