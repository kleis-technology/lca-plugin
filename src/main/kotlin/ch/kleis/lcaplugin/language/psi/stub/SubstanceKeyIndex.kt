package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.Substance
import ch.kleis.lcaplugin.language.psi.stub.LcaSubIndexKeys.SUBSTANCES
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class SubstanceKeyIndex : StringStubIndexExtension<Substance>() {
    override fun getKey(): StubIndexKey<String, Substance> =
        SUBSTANCES

    companion object {

        fun findSubstances(
            project: Project,
            target: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<Substance> =
            StubIndex.getElements(SUBSTANCES, target, project, scope, Substance::class.java)

    }
}

