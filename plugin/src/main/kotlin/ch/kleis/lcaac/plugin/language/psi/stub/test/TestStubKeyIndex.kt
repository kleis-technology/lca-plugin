package ch.kleis.lcaac.plugin.language.psi.stub.test

import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.language.psi.stub.stubIndexVersion
import ch.kleis.lcaac.plugin.psi.LcaTest
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class TestStubKeyIndex : StringStubIndexExtension<LcaTest>() {
    override fun getKey(): StubIndexKey<String, LcaTest> {
        return LcaStubIndexKeys.TESTS
    }

    override fun getVersion(): Int {
        return stubIndexVersion
    }

    companion object {
        fun findTest(
            project: Project,
            fqn: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<LcaTest> {
            return StubIndex.getElements(LcaStubIndexKeys.TESTS, fqn, project, scope, LcaTest::class.java)
        }

        fun findAllTests(
            project: Project,
        ): Collection<LcaTest> {
            return StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.TESTS, project)
                .flatMap {
                    findTest(project, it)
                }
        }
    }
}
