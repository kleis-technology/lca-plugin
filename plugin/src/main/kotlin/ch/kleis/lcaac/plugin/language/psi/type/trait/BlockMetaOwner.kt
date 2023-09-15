package ch.kleis.lcaac.plugin.language.psi.type.trait

import ch.kleis.lcaac.plugin.psi.LcaBlockMeta
import org.jetbrains.annotations.NotNull

interface BlockMetaOwner {
    @NotNull
    fun getBlockMetaList(): List<LcaBlockMeta>
}
