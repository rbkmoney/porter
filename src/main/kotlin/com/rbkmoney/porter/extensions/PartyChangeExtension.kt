package com.rbkmoney.porter.extensions

import com.rbkmoney.damsel.payment_processing.ClaimStatus
import com.rbkmoney.damsel.payment_processing.PartyChange

fun PartyChange.getClaimStatus(): ClaimStatus? {
    return when {
        isSetClaimCreated -> {
            claimCreated.status
        }
        isSetClaimStatusChanged -> {
            claimCreated.status
        }
        else -> null
    }
}
