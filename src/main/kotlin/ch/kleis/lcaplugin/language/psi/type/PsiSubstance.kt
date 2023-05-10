package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceStub
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockImpacts
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockMeta
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiImpactExchange
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.field.PsiSubstanceTypeField
import ch.kleis.lcaplugin.language.psi.type.field.PsiUnitField
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.language.psi.type.trait.BlockMetaOwner
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.TokenSet

class PsiSubstance :
    BlockMetaOwner,
    PsiNameIdentifierOwner,
    StubBasedPsiElementBase<SubstanceStub>,
    StubBasedPsiElement<SubstanceStub> {
    constructor(node: ASTNode) : super(node)
    constructor(stub: SubstanceStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    fun getSubstanceRef(): PsiSubstanceRef {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_substanceRef))?.psi as PsiSubstanceRef
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
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_nameField))?.psi as PsiStringLiteralField
    }

    fun getTypeField(): PsiSubstanceTypeField {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_typeField))?.psi as PsiSubstanceTypeField
    }

    fun getCompartmentField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_compartmentField))?.psi as PsiStringLiteralField
    }

    fun getSubcompartmentField(): PsiStringLiteralField? {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_subCompartmentField))?.psi as PsiStringLiteralField?
    }

    fun getReferenceUnitField(): PsiUnitField {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_referenceUnitField))?.psi as PsiUnitField
    }

    fun hasImpacts(): Boolean {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_block_impacts)) != null
    }

    fun getBlockImpacts(): Collection<PsiBlockImpacts> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_block_impacts)))
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
