package ch.kleis.lcaac.plugin.language.psi.stub.datasource

import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.language.psi.stub.stubIndexVersion
import ch.kleis.lcaac.plugin.psi.LcaDataSourceDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class DataSourceStubKeyIndex : StringStubIndexExtension<LcaDataSourceDefinition>() {
    override fun getKey(): StubIndexKey<String, LcaDataSourceDefinition> {
        return LcaStubIndexKeys.DATA_SOURCES
    }

    override fun getVersion(): Int {
        return stubIndexVersion
    }

    companion object {
        fun findDataSources(
            project: Project,
            fqn: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
        ): Collection<LcaDataSourceDefinition> =
            StubIndex.getElements(
                LcaStubIndexKeys.DATA_SOURCES,
                fqn,
                project,
                scope,
                LcaDataSourceDefinition::class.java
            )
    }
}
