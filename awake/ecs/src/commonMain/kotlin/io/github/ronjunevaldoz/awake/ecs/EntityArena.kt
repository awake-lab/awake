// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

@PublishedApi
internal class EntityArena {
    private var entityGenerations = IntArray(DEFAULT_CAPACITY)
    private var entityAlive = LongArray(DEFAULT_CAPACITY / 64 + 1)
    private var entitySignatures = LongArray(DEFAULT_CAPACITY)
    private var entitiesCount = 0

    private val recycledEntityIds = EntityIdStack()

    internal val count: Int
        get() = entitiesCount

    fun create(): Entity {
        val recycledId = recycledEntityIds.pop()
        if (recycledId >= 0) {
            setAlive(recycledId, true)
            return Entity.of(recycledId, entityGenerations[recycledId])
        }

        val nextId = entitiesCount++
        ensureCapacity(nextId)
        entityGenerations[nextId] = 0
        setAlive(nextId, true)
        return Entity.of(nextId, 0)
    }

    fun destroy(entity: Entity): Boolean {
        if (!isAlive(entity)) {
            return false
        }

        val id = entity.id
        setAlive(id, false)
        entityGenerations[id] += 1
        entitySignatures[id] = 0L
        recycledEntityIds.push(id)
        return true
    }

    fun isAlive(entity: Entity): Boolean {
        val id = entity.id
        if (id < 0 || id >= entitiesCount) return false
        return isAlive(id) && entityGenerations[id] == entity.generation
    }

    fun isAlive(id: Int): Boolean {
        val wordIndex = id ushr 6
        return if (wordIndex < entityAlive.size) {
            (entityAlive[wordIndex] and (1L shl (id and 63))) != 0L
        } else {
            false
        }
    }

    fun entity(id: Int): Entity {
        return Entity.of(id, entityGenerations[id])
    }

    fun signature(id: Int): Long {
        return if (id >= 0 && id < entitiesCount) entitySignatures[id] else 0L
    }

    fun has(entity: Entity, typeId: ComponentTypeId): Boolean {
        if (!isAlive(entity)) return false
        return (entitySignatures[entity.id] and (1L shl typeId.value)) != 0L
    }

    fun markComponentAdded(id: Int, typeId: ComponentTypeId) {
        entitySignatures[id] = entitySignatures[id] or (1L shl typeId.value)
    }

    fun markComponentRemoved(id: Int, typeId: ComponentTypeId) {
        entitySignatures[id] = entitySignatures[id] and (1L shl typeId.value).inv()
    }

    fun clear() {
        entitiesCount = 0
        entityGenerations.fill(0)
        entityAlive.fill(0L)
        entitySignatures.fill(0L)
        recycledEntityIds.clear()
    }

    private fun setAlive(id: Int, alive: Boolean) {
        val wordIndex = id ushr 6
        val bit = 1L shl (id and 63)
        if (alive) {
            entityAlive[wordIndex] = entityAlive[wordIndex] or bit
        } else {
            entityAlive[wordIndex] = entityAlive[wordIndex] and bit.inv()
        }
    }

    private fun ensureCapacity(id: Int) {
        if (id >= entityGenerations.size) {
            val newCapacity = maxOf(id + 1, entityGenerations.size * 2)
            entityGenerations = entityGenerations.copyOf(newCapacity)
            entitySignatures = entitySignatures.copyOf(newCapacity)
            entityAlive = entityAlive.copyOf(newCapacity / 64 + 1)
        }
    }

    private companion object {
        const val DEFAULT_CAPACITY = 16
    }
}
