package io.parrotsoftware.qatest.data.entities

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.parrotsoftware.qatest.data.domain.Product
import io.parrotsoftware.qatest.ui.list.ExpandableCategory
import org.jetbrains.annotations.NotNull

@Entity(tableName = ProductEntity.TABLE_NAME)
data class ProductEntity(
    @PrimaryKey @ColumnInfo(name = "id") @NotNull var id: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "image") var image: String,
    @ColumnInfo(name = "price") var price: Float,
    @ColumnInfo(name = "isAvailable") var isAvailable: Boolean,
    @ColumnInfo(name = "category") var category: CategoryEntity
) {
    companion object {
        const val TABLE_NAME = "expandableCategory"
    }
}

data class CategoryEntity(
    @SerializedName("idCaEn")
    var idCaEn: String,
    @SerializedName("nameCaEn")
    var nameCaEn: String,
    @SerializedName("position")
    var position: Int
)

class CategoryTypeConverter {

    @TypeConverter
    fun objectToGson(value: CategoryEntity): String = Gson().toJson(value)

    @TypeConverter
    fun jsonToObjet(value: String) =
        Gson().fromJson(value, CategoryEntity::class.java) as CategoryEntity

}
