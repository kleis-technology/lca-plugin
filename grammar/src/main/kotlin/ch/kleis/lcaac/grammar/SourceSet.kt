package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.EPackage
import ch.kleis.lcaac.grammar.parser.LcaLangParser

class SourceSet(
    files: Sequence<LcaLangParser.LcaFileContext>
) {
    private val filesByPkgName = files.groupBy {
        it.pkg()?.urn()?.text ?: EPackage.DEFAULT_PKG_NAME
    }

    fun pkgNames(): Set<String> = filesByPkgName.keys

    fun filesOf(pkgName: String): List<LcaLangParser.LcaFileContext> =
        filesByPkgName[pkgName] ?: throw EvaluatorException("unknown package $pkgName")
}
