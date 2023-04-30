package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.stub.techno_product_exchange.TechnoProductExchangeStub
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.IStubElementType

class PsiTechnoProductExchange : StubBasedPsiElementBase<TechnoProductExchangeStub>, PsiNameIdentifierOwner {
    constructor(node: ASTNode) : super(node)
    constructor(stub: TechnoProductExchangeStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    fun getProductRef(): PsiProductRef {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_productRef))?.psi as PsiProductRef
    }

    fun getQuantity(): PsiQuantity {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantity))?.psi as PsiQuantity
    }

    override fun getName(): String {
        return getProductRef().name
    }

    override fun setName(name: String): PsiElement {
        getProductRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getProductRef().nameIdentifier
    }
}
