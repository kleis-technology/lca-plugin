/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.math.dual

import org.jetbrains.kotlinx.multik.ndarray.data.D1Array


data class DualNumber(
    val zeroth: Double,
    val first: D1Array<Double>,
)
