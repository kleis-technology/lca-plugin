package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceStub
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockImpacts
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockMeta
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiImpactExchange
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.field.PsiSubstanceTypeField
import ch.kleis.lcaplugin.language.psi.type.field.PsiUnitField
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.language.psi.type.trait.BlockMetaOwner
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.TokenSet

class PsiSubstance : BlockMetaOwner, PsiNameIdentifierOwner, StubBasedPsiElementBase<SubstanceStub> {
    constructor(node: ASTNode) : super(node)
    constructor(stub: SubstanceStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    fun getSubstanceRef(): PsiSubstanceRef {
        return node.findChildByType(LcaElementTypes.SUBSTANCE_REF)?.psi as PsiSubstanceRef
    }

    override fun getName(): String {
        return getSubstanceRef().name
    }

    override fun getNameIdentifier(): PsiElement? {
        return getSubstanceRef().nameIdentifier
    }

    override fun setName(name: String): PsiElement {
        getSubstanceRef().name = name
        return this
    }

    fun getNameField(): PsiStringLiteralField {
        return node.findChildByType(LcaElementTypes.NAME_FIELD)?.psi as PsiStringLiteralField
    }

    fun getTypeField(): PsiSubstanceTypeField {
        return node.findChildByType(LcaElementTypes.TYPE_FIELD)?.psi as PsiSubstanceTypeField
    }

    fun getCompartmentField(): PsiStringLiteralField {
        return node.findChildByType(LcaElementTypes.COMPARTMENT_FIELD)?.psi as PsiStringLiteralField
    }

    fun getSubcompartmentField(): PsiStringLiteralField? {
        return node.findChildByType(LcaElementTypes.SUB_COMPARTMENT_FIELD)?.psi as PsiStringLiteralField?
    }

    fun getReferenceUnitField(): PsiUnitField {
        return node.findChildByType(LcaElementTypes.REFERENCE_UNIT_FIELD)?.psi as PsiUnitField
    }

    fun hasImpacts(): Boolean {
        return node.findChildByType(LcaElementTypes.BLOCK_IMPACTS) != null
    }

    fun getBlockImpacts(): Collection<PsiBlockImpacts> {
        return node.getChildren(TokenSet.create(LcaElementTypes.BLOCK_IMPACTS))
            .map { it.psi as PsiBlockImpacts }
    }

    fun getImpactExchanges(): Collection<PsiImpactExchange> {
        return getBlockImpacts()
            .flatMap { it.getExchanges() }
    }

    override fun getBlockMetaList(): List<PsiBlockMeta> {
        TODO("Not yet implemented")
    }
}
