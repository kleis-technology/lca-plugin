/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.value

data class FromProcessRefValue<Q>(
    val name: String,
    val matchLabels: Map<String, StringValue<Q>> = emptyMap(),
    val arguments: Map<String, DataValue<Q>> = emptyMap(),
)
