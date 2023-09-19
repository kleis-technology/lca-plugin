package ch.kleis.lcaac.plugin.language.parser

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.language.psi.index.LcaProcessFileIndex
import ch.kleis.lcaac.plugin.language.psi.type.spec.PsiProcessTemplateSpec
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

interface RefFileResolver {
    fun resolve(element: PsiElement): List<LcaFile>
}

class DefaultRefFileResolver(
    private val project: Project
) : RefFileResolver {
    override fun resolve(element: PsiElement): List<LcaFile> {
        val lcaFile = element.containingFile as LcaFile
        val packageCandidates = lcaFile.getImportNames()
            .plus(lcaFile.getPackageName())
        return when (element) {
            is PsiProcessTemplateSpec -> {
                LcaProcessFileIndex.findFiles(
                    project,
                    element.getProcessRef().name,
                ).toList()
            }

            else -> element.reference?.resolve()?.containingFile?.let { listOf(it as LcaFile) } ?: emptyList()
        }.filter { packageCandidates.contains(it.getPackageName()) }
    }
}
