package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.language.psi.stub.process.ProcessStub
import ch.kleis.lcaac.plugin.language.psi.type.trait.BlockMetaOwner
import ch.kleis.lcaac.plugin.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiProcess : StubBasedPsiElement<ProcessStub>, PsiNameIdentifierOwner, BlockMetaOwner {

    override fun getName(): String

    fun buildUniqueKey(): String

    fun getParameters(): Map<String, LcaDataExpression>

    fun getVariables(): Map<String, LcaDataExpression>

    fun getLabels(): Map<String, String>

    fun getProducts(): Collection<LcaTechnoProductExchange>

    fun getInputs(): Collection<LcaTechnoInputExchange>

    fun getEmissions(): Collection<LcaBioExchange>

    fun getLandUse(): Collection<LcaBioExchange>

    fun getResources(): Collection<LcaBioExchange>

    fun getImpacts(): Collection<LcaImpactExchange>
}
