package ch.kleis.lcaplugin.core.lang

import arrow.optics.Fold

interface IndexKeyDescriptor<K> {
    fun serialize(key: K): String
}

class Index<K, E> private constructor(
        private val indexType: String,
        private val indexKeyDescriptor: IndexKeyDescriptor<K>,
        private val cachedEntries: Map<String, E>
) {
    constructor(
            register: Register<E>,
            indexKeyDescriptor: IndexKeyDescriptor<K>,
            optics: Fold<E, K>
    ) : this(
            register.registerType,
            indexKeyDescriptor,
            register.getEntries(optics).mapKeys { indexKeyDescriptor.serialize(it.key) },
    )

    operator fun get(key: K): E? {
        return cachedEntries[indexKeyDescriptor.serialize(key)]
    }

    override fun toString(): String {
        return "[index<${indexType}>]"
    }

}
