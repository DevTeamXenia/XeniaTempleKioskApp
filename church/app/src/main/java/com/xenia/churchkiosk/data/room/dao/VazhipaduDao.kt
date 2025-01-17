package com.xenia.churchkiosk.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xenia.churchkiosk.data.network.model.DistinctPerson
import com.xenia.churchkiosk.data.network.model.OfferingItem
import com.xenia.churchkiosk.data.room.entity.Vazhipadu

@Dao
interface VazhipaduDao {

    @Query("SELECT * FROM vazhipadu")
    suspend fun getAllCart(): List<Vazhipadu>

    @Query("SELECT * FROM vazhipadu WHERE vaIsCompleted = 0")
    suspend fun getSelectedItems(): List<Vazhipadu>

    @Query("SELECT * FROM Vazhipadu WHERE vaName = :name")
    suspend fun selectToCompleteByName(name: String) : List<Vazhipadu>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(vazhipadu: Vazhipadu)

    @Query("DELETE FROM vazhipadu WHERE vaOfferingsId = :offeringsId")
    suspend fun deleteCartItemByOfferingId(offeringsId: Int)

    @Query("DELETE FROM vazhipadu")
    suspend fun truncateTable()

    @Query("SELECT COUNT(*) FROM vazhipadu")
    suspend fun getCartCount(): Int

    @Query("SELECT COUNT(*) FROM vazhipadu WHERE (vaName IS NULL OR vaName = '')")
    suspend fun getCountForEmptyOrNullName(): Int

    @Query("SELECT EXISTS (SELECT 1 FROM vazhipadu WHERE vaIsCompleted = 0)")
    suspend fun hasIncompleteItems(): Boolean

    @Query("SELECT COALESCE(SUM(vaOfferingsAmount), 0) FROM vazhipadu")
    suspend fun getTotalAmount(): Double

    @Query("UPDATE Vazhipadu SET vaName = :newName, vaIsCompleted = 1 WHERE vaIsCompleted = 0")
    suspend fun updateNameAndSetCompleted(
        newName: String
    )

    @Query("UPDATE Vazhipadu SET vaName = :newName WHERE vaIsCompleted = 0")
    suspend fun updateName(
        newName: String
    )

    @Query("UPDATE Vazhipadu SET vaIsCompleted = 0 WHERE vaName = :name ")
    suspend fun updateToIncompleteByNameAndStar(name: String)

    @Query("SELECT COUNT(DISTINCT vaName) FROM Vazhipadu")
    suspend fun getDistinctCountOfName(): Int

    @Query("SELECT DISTINCT vaName FROM Vazhipadu")
    suspend fun getDistinctPersons(): List<DistinctPerson>

    @Query("SELECT * FROM Vazhipadu WHERE vaName = :name")
    suspend fun getVazhipaduByPerson(name: String): List<OfferingItem>

    @Query("DELETE FROM Vazhipadu WHERE vaName = :name AND vaOfferingsId = :offeringId")
    suspend fun deleteOfferingByNameAndId(name: String, offeringId: Int)

}
