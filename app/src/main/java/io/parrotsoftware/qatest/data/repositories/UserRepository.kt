package io.parrotsoftware.qatest.data.repositories

import io.parrotsoftware.qatest.data.domain.Credentials
import io.parrotsoftware.qatest.data.domain.RepositoryResult
import io.parrotsoftware.qatest.data.domain.Store

interface UserRepository {

    suspend fun login(email: String, password: String): RepositoryResult<Nothing>

    suspend fun userExists(): RepositoryResult<Boolean>

    suspend fun getCredentials(): RepositoryResult<Credentials>

    suspend fun getStore(): RepositoryResult<Store>

    suspend fun clearUserData()

}