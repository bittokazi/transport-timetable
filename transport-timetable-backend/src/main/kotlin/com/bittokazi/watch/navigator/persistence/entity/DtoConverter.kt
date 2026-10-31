package com.bittokazi.watch.navigator.persistence.entity

interface DtoConverter<T> {
    fun toDto(): T
}
