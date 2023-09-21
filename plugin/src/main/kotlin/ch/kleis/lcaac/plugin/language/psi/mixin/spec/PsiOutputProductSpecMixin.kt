package ch.kleis.lcaac.plugin.language.psi.mixin.spec

import ch.kleis.lcaac.plugin.language.psi.stub.output_product.OutputProductStub
import ch.kleis.lcaac.plugin.psi.LcaOutputProductSpec
import ch.kleis.lcaac.plugin.psi.LcaProcess
import ch.kleis.lcaac.plugin.psi.LcaTechnoProductExchange
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil

abstract class PsiOutputProductSpecMixin : StubBasedPsiElementBase<OutputProductStub>, LcaOutputProductSpec {
    constructor(node: ASTNode) : super(node)
    constructor(stub: OutputProductStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    // XXX: Discuss with PBL on how to avoid using this method: any navigation "up" calls getNode() and forces a build
    // of the AST, which is *very* CPU and memory costly wrt down psi navigation.
    override fun getContainingProcess(): LcaProcess {
        return PsiTreeUtil.findFirstParent(this) { it is LcaProcess } as LcaProcess
    }

    override fun getContainingTechnoExchange(): LcaTechnoProductExchange {
        return PsiTreeUtil.findFirstParent(this) { it is LcaTechnoProductExchange } as LcaTechnoProductExchange
    }

    override fun getName(): String {
        return productRef.name
    }

    override fun setName(name: String): PsiElement {
        productRef.name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return productRef.nameIdentifier
    }
}
