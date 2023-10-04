package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.stub.process.ProcessStub
import ch.kleis.lcaac.plugin.psi.*
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType

abstract class PsiProcessMixin : StubBasedPsiElementBase<ProcessStub>, LcaProcess {
    constructor(node: ASTNode) : super(node)
    constructor(stub: ProcessStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun buildUniqueKey(): String =
        if (this.getLabels().isEmpty()) this.name else "${this.name}${this.getLabels()}"

    override fun getName(): String {
        return processRef.name
    }

    override fun setName(name: String): PsiElement {
        processRef.name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return processRef.nameIdentifier
    }

    override fun getParameters(): Map<String, LcaDataExpression> {
        return paramsList
            .flatMap {
                it.guardedAssignmentList.map(LcaGuardedAssignment::getAssignment).map { a ->
                    a.getDataRef().name to a.getValue()
                }
            }
            .toMap()
    }

    override fun getProducts(): Collection<LcaTechnoProductExchange> {
        return blockProductsList
            .flatMap { it.technoProductExchangeList }
    }

    override fun getInputs(): Collection<LcaTechnoInputExchange> {
        return blockInputsList
            .flatMap { it.technoInputExchangeList }
    }

    override fun getLabels(): Map<String, String> {
        return labelsList
            .flatMap { it.labelAssignmentList }
            .associate { it.name to it.getValue() }
    }

    override fun getEmissions(): Collection<LcaBioExchange> {
        return blockEmissionsList
            .flatMap { it.bioExchangeList }
    }

    override fun getLandUse(): Collection<LcaBioExchange> {
        return blockLandUseList
            .flatMap { it.bioExchangeList }
    }

    override fun getResources(): Collection<LcaBioExchange> {
        return blockResourcesList
            .flatMap { it.bioExchangeList }
    }

    override fun getImpacts(): Collection<LcaImpactExchange> {
        return blockImpactsList
            .flatMap { it.impactExchangeList }
    }

    override fun getVariables(): Map<String, LcaDataExpression> {
        return variablesList
            .flatMap {
                it.assignmentList.map { a ->
                    a.getDataRef().name to a.getValue()
                }
            }
            .toMap()
    }

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        for (block in getLabelsList()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }

        for (block in getVariablesList()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }

        for (block in getParamsList()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }

        return true
    }
}
