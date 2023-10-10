package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.stub.run.RunStub
import ch.kleis.lcaac.plugin.psi.LcaRun
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class PsiRunMixin : StubBasedPsiElementBase<RunStub>, LcaRun {

    constructor(node: ASTNode) : super(node)
    constructor(stub: RunStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String {
        return runRef.name
    }

//    fun getFullyQualifiedName(): String {
//        val pkgName = (containingFile as LcaFile).getPackageName()
//        return "$pkgName.${this.name}"
//    }

//    override fun processDeclarations(
//        processor: PsiScopeProcessor,
//        state: ResolveState,
//        lastParent: PsiElement?,
//        place: PsiElement
//    ): Boolean {
//        for (block in getVariablesList()) {
//            if (!processor.execute(block, state)) {
//                return false
//            }
//        }
//        return true
//    }
}
