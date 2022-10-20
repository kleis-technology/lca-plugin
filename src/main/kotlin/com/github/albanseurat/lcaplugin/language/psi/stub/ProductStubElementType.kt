package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.LcaLanguage
import com.github.albanseurat.lcaplugin.language.psi.type.Product
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.github.albanseurat.lcaplugin.psi.impl.LcaProductImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.stubs.*

class ProductStubElementType(debugName: String) : ILightStubElementType<ProductStub,
        Product>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String = "lca.${super.toString()}"

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ProductStub {
        return ProductStubImpl(parentStub as StubElement<Product>, dataStream.readNameString());
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ProductStub {
        val keyNode = LightTreeUtil.firstChildOfType(tree, node, LcaTypes.IDENTIFIER) as LighterASTTokenNode
        return ProductStubImpl(
            parentStub as StubElement<Product>,
            tree.charTable.intern(keyNode.text).toString()
        );
    }

    override fun createStub(psi: Product, parentStub: StubElement<out PsiElement>?): ProductStub {
        return ProductStubImpl(parentStub as StubElement<Product>, psi.name)
    }

    override fun createPsi(stub: ProductStub): Product {
        return LcaProductImpl(stub, this);
    }

    override fun indexStub(stub: ProductStub, sink: IndexSink) {
        sink.occurrence(LcaSubIndexKeys.PRODUCTS, stub.productName!!);
    }

    override fun serialize(stub: ProductStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.productName);
    }


}
