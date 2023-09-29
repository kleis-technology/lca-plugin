/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core

interface HasUID {
    fun getUID(): String {
        return "${hashCode()}"
    }
}

data class ParameterName(
    val uid: String
) : HasUID {
    override fun getUID(): String {
        return uid
    }
}
