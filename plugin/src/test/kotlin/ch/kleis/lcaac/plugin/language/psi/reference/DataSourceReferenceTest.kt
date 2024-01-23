package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.stub.datasource.DataSourceStubKeyIndex
import ch.kleis.lcaac.plugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DataSourceReferenceTest: BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun test_resolve_whenDataSource() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile("$pkgName.lca", """
            package $pkgName
            
            datasource inventory {
                location = "inventory.csv"
                schema {
                    "mass" = 1 kg
                }
            }
            
            process p {
                params {
                    row from inventory
                }
            }
        """.trimIndent())
        val dataSource = DataSourceStubKeyIndex.findDataSources(
            project, "$pkgName.inventory"
        ).first()
        val ref = ProcessStubKeyIndex.findProcesses(
            project, "$pkgName.p"
        ).first()
            .paramsList.first()
            .assignmentList.first()
            .dataSourceRef!!

        // when
        val actual = ref.reference.resolve()

        // then
        assertEquals(dataSource, actual)
    }
}
