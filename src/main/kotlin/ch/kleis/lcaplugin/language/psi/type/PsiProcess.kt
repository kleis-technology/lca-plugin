package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStub
import ch.kleis.lcaplugin.language.psi.type.block.*
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiBioExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchangeWithAllocateField
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import ch.kleis.lcaplugin.language.psi.type.trait.BlockMetaOwner
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.ResolveState
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.TokenSet

/**
 * StubBasedPsiElementBase<X> and StubBasedPsiElement<X> are not related.
 */
class PsiProcess :
    StubBasedPsiElementBase<ProcessStub>,
    StubBasedPsiElement<ProcessStub>,
    PsiNameIdentifierOwner,
    BlockMetaOwner {
    constructor(node: ASTNode) : super(node)
    constructor(stub: ProcessStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    fun getProcessTemplateRef(): PsiProcessTemplateRef {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_processTemplateRef))?.psi as PsiProcessTemplateRef
    }

    override fun getName(): String {
        return getProcessTemplateRef().name
    }

    override fun setName(name: String): PsiElement {
        getProcessTemplateRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getProcessTemplateRef().nameIdentifier
    }

    fun getParameters(): Map<String, PsiQuantity> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_params)))
            .map { it.psi as PsiParameters }
            .flatMap { it.getEntries() }
            .toMap()
    }

    fun getProducts(): Collection<PsiTechnoProductExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_block_products)))
            .map { it.psi as PsiBlockProducts }
            .flatMap { it.getExchanges() }
    }

    fun getProductsWithAllocation(): Collection<PsiTechnoProductExchangeWithAllocateField> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_block_products)))
            .map { it.psi as PsiBlockProducts }
            .flatMap { it.getExchangesWithAllocateField() }
    }

    fun getInputs(): Collection<PsiTechnoInputExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_block_inputs)))
            .map { it.psi as PsiBlockInputs }
            .flatMap { it.getExchanges() }
    }

    fun getEmissions(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_block_emissions)))
            .map { it.psi as PsiBlockEmissions }
            .flatMap { it.getExchanges() }
    }

    fun getLandUse(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_block_land_use)))
            .map { it.psi as PsiBlockLandUse }
            .flatMap { it.getExchanges() }
    }

    fun getResources(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_block_resources)))
            .map { it.psi as PsiBlockResources }
            .flatMap { it.getExchanges() }
    }

    fun getVariables(): Map<String, PsiQuantity> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_variables)))
            .map { it.psi as PsiVariables }
            .flatMap { it.getEntries() }
            .toMap()
    }

    fun getPsiVariablesBlocks(): Collection<PsiVariables> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_variables)))
            .map { it.psi as PsiVariables }
    }

    fun getPsiParametersBlocks(): Collection<PsiParameters> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_params)))
            .map { it.psi as PsiParameters }
    }

    override fun getBlockMetaList(): List<PsiBlockMeta> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_block_meta)))
            .map { it.psi as PsiBlockMeta }
    }

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        for (block in getPsiVariablesBlocks()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }

        for (block in getPsiParametersBlocks()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }

        return true
    }

}
