package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.PartyEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PartyRepository : CrudRepository<PartyEntity, Long>
