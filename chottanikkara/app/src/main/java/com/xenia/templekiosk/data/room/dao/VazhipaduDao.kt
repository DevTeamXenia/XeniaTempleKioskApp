package com.xenia.templekiosk.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xenia.templekiosk.data.network.model.DistinctPerson
import com.xenia.templekiosk.data.network.model.OfferingItem
import com.xenia.templekiosk.data.room.entity.Vazhipadu

@Dao
interface VazhipaduDao {

    @Query("SELECT * FROM vazhipadu")
    suspend fun getAllCart(): List<Vazhipadu>

    @Query("SELECT * FROM vazhipadu WHERE vaIsCompleted = 0")
    suspend fun getSelectedItems(): List<Vazhipadu>

    @Query("SELECT * FROM Vazhipadu WHERE vaName = :name AND vaStar = :star AND vaSubTempleId = :devathaId")
    suspend fun selectToCompleteByNameAndStar(name: String, star: String, devathaId: Int) : List<Vazhipadu>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(vazhipadu: Vazhipadu)

    @Query("DELETE FROM vazhipadu WHERE vaOfferingsId = :offeringsId AND vaSubTempleId = :selectedCardId")
    suspend fun deleteCartItemByOfferingId(offeringsId: Int, selectedCardId: Int?)

    @Query("DELETE FROM vazhipadu")
    suspend fun truncateTable()

    @Query("SELECT COUNT(*) FROM vazhipadu")
    suspend fun getCartCount(): Int

    @Query("SELECT COUNT(*) FROM vazhipadu WHERE (vaName IS NULL OR vaName = '') OR (vaStar IS NULL OR vaStar = '')")
    suspend fun getCountForEmptyOrNullNameAndStar(): Int

    @Query("SELECT EXISTS (SELECT 1 FROM vazhipadu WHERE vaIsCompleted = 0)")
    suspend fun hasIncompleteItems(): Boolean

    @Query("SELECT COALESCE(SUM(vaOfferingsAmount), 0) FROM vazhipadu")
    suspend fun getTotalAmount(): Double

    @Query("UPDATE Vazhipadu SET vaName = :newName, vaIsCompleted = 1 WHERE vaIsCompleted = 0")
    suspend fun updateNameAndSetCompleted(newName: String)

    @Query("UPDATE Vazhipadu SET vaIsCompleted = 0 WHERE vaName = :name AND vaStar = :star")
    suspend fun updateToIncompleteByNameAndStar(name: String, star: String)

    @Query("SELECT COUNT(DISTINCT vaName || vaStar) FROM Vazhipadu")
    suspend fun getDistinctCountOfNameAndStar(): Int

    @Query("SELECT DISTINCT vaName, vaStar, vaStarLa FROM Vazhipadu")
    suspend fun getDistinctPersons(): List<DistinctPerson>

    @Query("SELECT * FROM Vazhipadu WHERE vaName = :name AND vaStar = :star")
    suspend fun getVazhipaduByPerson(name: String, star: String): List<OfferingItem>

    @Query("DELETE FROM Vazhipadu WHERE vaName = :name AND vaStar = :star AND vaOfferingsId = :offeringId")
    suspend fun deleteOfferingByNameStarAndId(name: String, star: String, offeringId: Int)

}
