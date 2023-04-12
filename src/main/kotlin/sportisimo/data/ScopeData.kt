package sportisimo.data

import sportisimo.azure.Scope

data class ScopeData(
    val scopeType: Scope,
    val isRequired: Boolean,
    val scopeName: String,
    val scopeValue: String
)