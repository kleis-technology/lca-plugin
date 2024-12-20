package ch.kleis.lcaac.plugin.language.loader

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.LcaColumnRef
import ch.kleis.lcaac.plugin.psi.LcaDataRef
import ch.kleis.lcaac.plugin.psi.LcaDataSourceRef
import ch.kleis.lcaac.plugin.psi.LcaInputProductSpec
import ch.kleis.lcaac.plugin.psi.LcaProcessTemplateSpec
import ch.kleis.lcaac.plugin.psi.LcaSubstanceSpec
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil

class LcaFileCollector(
    private val project: Project,
    private val refFileResolver: RefFileResolver = AccumulatingRefFileResolver(project)
) {
    companion object {
        private val LOG = Logger.getInstance(LcaFileCollector::class.java)
    }

    private val guard = CacheGuard { el: PsiNamedElement -> refFileResolver.resolve(el) }

    fun collect(file: LcaFile): Sequence<LcaFile> { // TODO Collect Symbols instead of files
        val result = HashMap<String, LcaFile>()
        LOG.info("Start recursive collect")
        recursiveCollect(result, mutableMapOf(file.virtualFile.path to file))
        LOG.info("End recursive collect, found ${result.size} entries")
        return result.values.asSequence()
    }


    private tailrec fun recursiveCollect(
        accumulator: MutableMap<String, LcaFile>,
        toVisit: MutableMap<String, LcaFile>
    ) {
        if (toVisit.isEmpty()) return
        val path = toVisit.keys.first()
        val file = toVisit.remove(path)!!
        val maybeVisited = accumulator[path]
        if (maybeVisited == null) {
            accumulator[path] = file
            val newDeps = dependenciesOf(file)
                .map { it.virtualFile.path to it }
                .filter { (p, _) -> !accumulator.containsKey(p) }
                .associateTo(toVisit) { it }
            recursiveCollect(accumulator, newDeps)
        } else {
            recursiveCollect(accumulator, toVisit)
        }
    }

    private fun dependenciesOf(file: LcaFile): Sequence<LcaFile> {
        return allReferences(file)
            .flatMap { el -> resolve(el) }
    }

    private fun resolve(element: PsiNamedElement): List<LcaFile> {
        return guard.guarded(element) ?: emptyList()
    }

    private fun allReferences(file: LcaFile): Sequence<PsiNamedElement> {
        return PsiTreeUtil.findChildrenOfAnyType(
            file,
            LcaSubstanceSpec::class.java,
            LcaDataRef::class.java,
            LcaDataSourceRef::class.java,
            LcaColumnRef::class.java,
            LcaInputProductSpec::class.java,
            LcaProcessTemplateSpec::class.java,
        ).asSequence()
    }
}

private class CacheGuard<E, R>(private val fnToGuard: (E) -> R?) where E : PsiNamedElement {
    private val visited = HashSet<String>()

    val guarded: (E) -> R? = { element: E ->
        val key = element.containingFile.virtualFile.path + "/${element.javaClass.simpleName}.${element.name}"
        if (!visited.contains(key)) {
            visited.add(key)
            fnToGuard(element)
        } else {
            null
        }
    }

}
