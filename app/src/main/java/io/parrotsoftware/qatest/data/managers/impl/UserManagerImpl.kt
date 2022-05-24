package io.parrotsoftware.qatest.data.managers.impl

import android.content.Context
import io.parrotsoftware.qatest.common.PrefsStorage
import io.parrotsoftware.qatest.data.managers.UserManager

class UserManagerImpl(ctx: Context) : UserManager, PrefsStorage(ctx, "ParrotPrefs") {

    override fun saveCredentials(access: String, refresh: String) {
        setString(KEY_ACCESS, access)
        setString(KEY_REFRESH, refresh)
    }

    override fun getAccess() = getString(KEY_ACCESS)

    override fun getRefresh() = getString(KEY_REFRESH)

    override fun saveStore(uuid: String, name: String) {
        setString(KEY_STORE_UUID, uuid)
        setString(KEY_STORE_NAME, name)
    }

    override fun getStoreUuid() = getString(KEY_STORE_UUID)

    override fun getStoreName() = getString(KEY_STORE_NAME)

    override fun isAuth() = getString(KEY_ACCESS).isNotBlank()

    override fun clearData() {
        clear()
    }

    private companion object {
        const val KEY_ACCESS = "key_access"
        const val KEY_REFRESH = "key_refresh"
        const val KEY_STORE_UUID = "key_store_uuid"
        const val KEY_STORE_NAME = "key_store_name"
    }

}