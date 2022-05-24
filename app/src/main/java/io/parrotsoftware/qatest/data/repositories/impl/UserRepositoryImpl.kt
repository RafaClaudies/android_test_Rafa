package io.parrotsoftware.qatest.data.repositories.impl

import io.parrotsoftware.qa_network.domain.requests.ApiAuthRequest
import io.parrotsoftware.qa_network.interactors.NetworkInteractor
import io.parrotsoftware.qa_network.services.ParrotApi
import io.parrotsoftware.qatest.data.domain.Credentials
import io.parrotsoftware.qatest.data.domain.RepositoryResult
import io.parrotsoftware.qatest.data.domain.Store
import io.parrotsoftware.qatest.data.managers.UserManager
import io.parrotsoftware.qatest.data.repositories.UserRepository

class UserRepositoryImpl(
    private val userManager: UserManager,
    private val networkInteractor: NetworkInteractor
) : UserRepository {

    override suspend fun login(email: String, password: String): RepositoryResult<Nothing> {
        val responseAuth = networkInteractor.safeApiCall {
            ParrotApi.service.auth(ApiAuthRequest(email, password))
        }
        if (responseAuth.isError)
            return RepositoryResult(
                errorCode = responseAuth.requiredError.requiredErrorCode,
                errorMessage = responseAuth.requiredError.requiredErrorMessage
            )

        val credentials = responseAuth.requiredResult
        val responseUser = networkInteractor.safeApiCall {
            ParrotApi.service.getMe("Bearer ${credentials.accessToken}")
        }

        if (responseUser.isError) {
            return RepositoryResult(
                errorCode = responseUser.requiredError.requiredErrorCode,
                errorMessage = responseUser.requiredError.requiredErrorMessage
            )
        }

        if (responseUser.requiredResult.result.stores.isEmpty()) {
            return RepositoryResult(
                errorCode = "",
                errorMessage = "Store Not Found"
            )
        }

        val apiCredentials = responseAuth.requiredResult
        val apiUser = responseUser.requiredResult.result
        val apiStore = apiUser.stores.first()

        userManager.saveCredentials(apiCredentials.accessToken, apiCredentials.refreshToken)
        userManager.saveStore(apiStore.uuid, apiStore.name)

        return RepositoryResult()
    }

    override suspend fun userExists(): RepositoryResult<Boolean> {
        return RepositoryResult(userManager.isAuth())
    }

    override suspend fun getCredentials(): RepositoryResult<Credentials> {
        return RepositoryResult(Credentials(
            userManager.getAccess(), userManager.getRefresh()
        ))
    }

    override suspend fun getStore(): RepositoryResult<Store> {
        return RepositoryResult(Store(
            userManager.getStoreUuid(), userManager.getStoreName()
        ))
    }

    override suspend fun clearUserData() {
        userManager.clearData()
    }
}