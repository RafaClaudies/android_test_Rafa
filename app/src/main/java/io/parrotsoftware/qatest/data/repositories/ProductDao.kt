package io.parrotsoftware.qatest.data.repositories

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.parrotsoftware.qatest.data.entities.ProductEntity

@Dao
interface ProductDao {

    @Insert
    fun insert(productEntity: ProductEntity)

    @Query("DELETE FROM " + ProductEntity.TABLE_NAME)
    fun clearTable()

    @Query("SELECT * FROM " + ProductEntity.TABLE_NAME)
    fun getAllCategories(): MutableList<ProductEntity>

}