package ch.kleis.lcaplugin.language.psi.type.trait

import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockMeta
import org.jetbrains.annotations.NotNull

interface BlockMetaOwner {
    @NotNull
    fun getBlockMetaList(): List<PsiBlockMeta>
}
