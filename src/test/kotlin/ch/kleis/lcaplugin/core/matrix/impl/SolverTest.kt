package ch.kleis.lcaplugin.core.matrix.impl

import ch.kleis.lcaplugin.core.matrix.MatrixFixture
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import java.lang.IllegalArgumentException

class SolverTest {
    private val precision = 1e-6

    @Test
    fun solve_whenNormal() {
        // given
        val a = MatrixFixture.make(
            2, 2, arrayOf(
                2.0, 0.0,
                0.0, 4.0,
            )
        )
        val b = MatrixFixture.make(
            2, 3, arrayOf(
                1.0, 0.0, 0.0,
                0.0, 2.0, 0.0,
            )
        )

        // when
        val c = Solver.INSTANCE.solve(a, b)!!

        // then
        assertMatrixEqual(
            c, arrayOf(
                0.5, 0.0, 0.0,
                0.0, 0.5, 0.0,
            )
        )
    }

    @Test()
    fun solve_whenZeroCols() {
        // given
        val a = MatrixFactory.INSTANCE.zero(0, 3)
        val b = MatrixFixture.make(
            3, 1, arrayOf(
                1.0,
                2.0,
                3.0
            )
        )

        // expect
        try {
            Solver.INSTANCE.solve(a, b)!!
            fail("should have thrown IllegalArgumentException")
        } catch (e : IllegalArgumentException) {
            assertEquals("requirement failed: CSCMatrix Multiplication Dimension Mismatch: a.cols == b.rows (0 != 3)", e.message)
        }

    }

    @Test
    fun solve_whenNonInvertible() {
        // given
        val a = MatrixFixture.make(
            3, 3, arrayOf(
                1.0, -2.0, 0.0,
                0.0, 1.0, 0.0,
                0.0, 0.0, 0.0,
            )
        )
        val b = MatrixFixture.make(
            2, 2, arrayOf(
                1.0, 4.0,
                1.0, 2.0,
            )
        )

        // expect
        try {
            Solver.INSTANCE.solve(a, b)!!
            fail("should have thrown IllegalArgumentException")
        } catch (e : IllegalArgumentException) {
            assertEquals("requirement failed: CSCMatrix Multiplication Dimension Mismatch: a.cols == b.rows (3 != 2)", e.message)
        }
    }

    private fun assertMatrixEqual(actual: Matrix, expected: Array<Double>) {
        assertEquals(expected.size, actual.rowDim() * actual.colDim())
        for (row in 0 until actual.rowDim()) {
            for (col in 0 until actual.colDim()) {
                assertEquals(
                    "(row ${row}, col ${col}):",
                    expected[row * actual.colDim() + col],
                    actual.value(row, col),
                    precision
                )
            }
        }
    }

}
