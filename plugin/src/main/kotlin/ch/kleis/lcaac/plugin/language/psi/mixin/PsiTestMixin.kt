package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.language.psi.stub.test.TestStub
import ch.kleis.lcaac.plugin.psi.LcaTest
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType

abstract class PsiTestMixin : StubBasedPsiElementBase<TestStub>, LcaTest {

    constructor(node: ASTNode) : super(node)
    constructor(stub: TestStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String {
        return testRef.name
    }

    fun getFullyQualifiedName(): String {
        val pkgName = (containingFile as LcaFile).getPackageName()
        return "$pkgName.${this.name}"
    }

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        for (block in getVariablesList()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }
        return true
    }
}
