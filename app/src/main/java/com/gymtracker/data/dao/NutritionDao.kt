package com.gymtracker.data.dao

import androidx.room.*
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.FoodItem
import com.gymtracker.data.entities.NutritionGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    @Query("SELECT * FROM food_items ORDER BY name ASC")
    fun getAllFoodItems(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%'")
    suspend fun searchFoodItems(query: String): List<FoodItem>

    @Insert
    suspend fun insertFoodItem(item: FoodItem): Long

    @Delete
    suspend fun deleteFoodItem(item: FoodItem)

    @Query("SELECT * FROM food_entries WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date ASC")
    fun getEntriesForDay(startOfDay: Long, endOfDay: Long): Flow<List<FoodEntry>>

    @Insert
    suspend fun insertEntry(entry: FoodEntry): Long

    @Delete
    suspend fun deleteEntry(entry: FoodEntry)

    @Query("SELECT * FROM nutrition_goals WHERE id = 1")
    fun getGoal(): Flow<NutritionGoal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: NutritionGoal)
}
