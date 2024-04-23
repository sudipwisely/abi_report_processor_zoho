package bi.accounting.model

import io.micronaut.core.annotation.NonNull

interface Identified {
    val id: @NonNull String?
}