package ch.kleis.lcaac.plugin.core.math.dual

import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array

data class DualMatrix(
    val zeroth: D2Array<Double>,
    val first: D3Array<Double>,
)
