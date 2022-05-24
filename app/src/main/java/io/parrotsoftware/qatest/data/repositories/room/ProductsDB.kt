package io.parrotsoftware.qatest.data.repositories.room

import android.content.Context
import androidx.room.*
import io.parrotsoftware.qatest.data.entities.CategoryTypeConverter
import io.parrotsoftware.qatest.data.entities.ProductEntity
import io.parrotsoftware.qatest.data.repositories.ProductDao

@Database(entities = [ProductEntity::class], version = 1)
@TypeConverters(CategoryTypeConverter::class)
abstract class ProductsDB : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        private const val DATABASE_NAME = "products_database"

        @Volatile
        private var INSTANCE: ProductsDB? = null

        fun getInstance(context: Context): ProductsDB? {
            INSTANCE ?: synchronized(this) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    ProductsDB::class.java,
                    DATABASE_NAME
                ).build()
            }
            return INSTANCE
        }
    }

}