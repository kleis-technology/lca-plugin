/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

@file:Suppress("NOTHING_TO_INLINE")

package ch.kleis.lcaac.core.lang.expression.optics

import arrow.core.identity
import arrow.core.left
import arrow.core.right
import ch.kleis.lcaac.core.lang.expression.*


inline fun <Q> LcaExpression.Companion.eProcess(): arrow.optics.Prism<LcaExpression<Q>, EProcess<Q>> =
    arrow.optics.Prism(
        getOrModify = { lcaExpression: LcaExpression<Q> ->
            when (lcaExpression) {
                is EProcess<Q> -> lcaExpression.right()
                else -> lcaExpression.left()
            }
        },
        reverseGet = ::identity
    )

inline fun <Q> LcaExpression.Companion.eSubstanceCharacterization(): arrow.optics.Prism<LcaExpression<Q>, ESubstanceCharacterization<Q>> =
    arrow.optics.Prism(
        getOrModify = { lcaExpression: LcaExpression<Q> ->
            when (lcaExpression) {
                is ESubstanceCharacterization<Q> -> lcaExpression.right()
                else -> lcaExpression.left()
            }
        },
        reverseGet = ::identity,
    )

inline fun <Q> LcaExpression.Companion.eProductSpec(): arrow.optics.Prism<LcaExpression<Q>, EProductSpec<Q>> =
    arrow.optics.Prism(
        getOrModify = { lcaExpression: LcaExpression<Q> ->
            when (lcaExpression) {
                is EProductSpec<Q> -> lcaExpression.right()
                else -> lcaExpression.left()
            }
        },
        reverseGet = ::identity
    )


inline fun <Q> LcaExpression.Companion.lcaExchangeExpression(): arrow.optics.Prism<LcaExpression<Q>, LcaExchangeExpression<Q>> =
    arrow.optics.Prism(
        getOrModify = { lcaExpression: LcaExpression<Q> ->
            when (lcaExpression) {
                is LcaExchangeExpression -> lcaExpression.right()
                else -> lcaExpression.left()
            }
        },
        reverseGet = ::identity
    )
