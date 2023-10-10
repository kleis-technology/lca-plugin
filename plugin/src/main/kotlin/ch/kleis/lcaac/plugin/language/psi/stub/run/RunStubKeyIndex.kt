package ch.kleis.lcaac.plugin.language.psi.stub.run

import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.language.psi.stub.stubIndexVersion
import ch.kleis.lcaac.plugin.psi.LcaRun
import ch.kleis.lcaac.plugin.psi.LcaTest
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class RunStubKeyIndex : StringStubIndexExtension<LcaRun>() {
    override fun getKey(): StubIndexKey<String, LcaRun> {
        return LcaStubIndexKeys.RUNS
    }

    override fun getVersion(): Int {
        return stubIndexVersion
    }

    companion object {
        fun findRun(
            project: Project,
            fqn: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<LcaRun> {
            return StubIndex.getElements(LcaStubIndexKeys.RUNS, fqn, project, scope, LcaRun::class.java)
        }

        fun findAllRuns(
            project: Project,
        ): Collection<LcaRun> {
            return StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.RUNS, project)
                .flatMap {
                    findRun(project, it)
                }
        }
    }
}
