package ch.kleis.lcaac.plugin.language.loader

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.language.psi.index.LcaProcessFileIndex
import ch.kleis.lcaac.plugin.psi.LcaProcessTemplateSpec
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
            is LcaProcessTemplateSpec -> {
                LcaProcessFileIndex.findFiles(
                    project,
                    element.getProcessRef().name,
                ).toList()
            }

            else -> element.reference?.resolve()?.containingFile?.let { listOf(it as LcaFile) } ?: emptyList()
        }.filter { packageCandidates.contains(it.getPackageName()) }
    }
}

class AccumulatingRefFileResolver(
    project: Project
) : RefFileResolver {
    private val elementMap: HashMap<PsiElement, List<LcaFile>> = HashMap()
    private val defaultResolver = DefaultRefFileResolver(project)

    override fun resolve(element: PsiElement): List<LcaFile> {
        return elementMap[element] ?: defaultResolver.resolve(element).also { elementMap[element] = it }
    }
}
