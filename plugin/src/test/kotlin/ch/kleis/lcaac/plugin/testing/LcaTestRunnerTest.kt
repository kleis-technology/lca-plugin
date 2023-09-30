package ch.kleis.lcaac.plugin.testing

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class LcaTestRunnerTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun test_run_whenMultipleOccurrencesOfSameIntermediateProduct_shouldSum() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile("$pkgName.lca",
            """
                process p {
                    products {
                        1 kg out
                    }
                    inputs {
                        1 kg intermediate from q(x = 1 kg)
                        1 kg intermediate from q(x = 2 kg)
                    }
                }
                
                process q {
                    params {
                        x = 1 kg
                    }
                    products {
                        1 kg intermediate
                    }
                    impacts {
                        x GWP
                    }
                }
                
                test p {
                    given {
                        2 kg out from p
                    }
                    assert {
                        intermediate between 4 kg and 4.1 kg
                        GWP between 6 kg and 6.1 kg
                    }
                }
            """.trimIndent())
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val target = file.getTests().first()
        val runner = LcaTestRunner(project)

        // when
        val result = runner.run(target)

        // then
        assertEquals(LcaTestSuccess, result)
    }

    @Test
    fun test_run_whenIntermediateProduct() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile("$pkgName.lca",
            """
                process p {
                    products {
                        1 kg out
                    }
                    inputs {
                        1 kg intermediate from q
                    }
                }
                
                process q {
                    products {
                        1 kg intermediate
                    }
                    impacts {
                        1 kg GWP
                    }
                }
                
                test p {
                    given {
                        2 kg out from p
                    }
                    assert {
                        intermediate between 500 g and 2 kg
                    }
                }
            """.trimIndent())
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val target = file.getTests().first()
        val runner = LcaTestRunner(project)

        // when
        val result = runner.run(target)

        // then
        assertEquals(LcaTestSuccess, result)
    }

    @Test
    fun test_run_whenSuccess() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile("$pkgName.lca",
            """
                process p {
                    products {
                        1 kg out
                    }
                    impacts {
                        1 kg GWP
                    }
                }
                
                test p {
                    given {
                        2 kg out from p
                    }
                    assert {
                        GWP between 500 g and 2 kg
                    }
                }
            """.trimIndent())
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val target = file.getTests().first()
        val runner = LcaTestRunner(project)

        // when
        val result = runner.run(target)

        // then
        assertEquals(LcaTestSuccess, result)
    }

    @Test
    fun test_run_whenFailure() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile("$pkgName.lca",
            """
                process p {
                    products {
                        1 kg out
                    }
                    impacts {
                        1 kg GWP
                    }
                }
                
                test p {
                    given {
                        2 kg out from p
                    }
                    assert {
                        GWP between 500 kg and 2000 kg
                    }
                }
            """.trimIndent())
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val target = file.getTests().first()
        val runner = LcaTestRunner(project)

        // when
        val result = runner.run(target)

        // then
        assertEquals(LcaTestFailure, result)
    }
}
