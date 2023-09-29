/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.expression.optics

import ch.kleis.lcaac.core.lang.expression.*

fun <Q> everyProcessTemplateInTemplateExpression() = Merge(
    listOf(
        ProcessTemplateExpression.eProcessTemplateApplication<Q>().template(),
        ProcessTemplateExpression.eProcessTemplate(),
    )
)