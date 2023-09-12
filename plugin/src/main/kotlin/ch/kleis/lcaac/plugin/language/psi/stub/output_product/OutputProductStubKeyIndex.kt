package ch.kleis.lcaac.plugin.language.psi.stub.output_product

import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.language.psi.stub.stubIndexVersion
import ch.kleis.lcaac.plugin.psi.LcaOutputProductSpec
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class OutputProductStubKeyIndex : StringStubIndexExtension<LcaOutputProductSpec>() {
    override fun getKey(): StubIndexKey<String, LcaOutputProductSpec> {
        return LcaStubIndexKeys.OUTPUT_PRODUCTS
    }

    override fun getVersion(): Int {
        return stubIndexVersion
    }

    object Util {
        fun findOutputProducts(
            project: Project,
            fqn: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<LcaOutputProductSpec> =
            StubIndex.getElements(
                LcaStubIndexKeys.OUTPUT_PRODUCTS,
                fqn,
                project,
                scope,
                LcaOutputProductSpec::class.java,
            )
    }

}
