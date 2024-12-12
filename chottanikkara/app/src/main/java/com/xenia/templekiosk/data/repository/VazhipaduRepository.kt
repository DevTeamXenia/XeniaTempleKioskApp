package com.xenia.templekiosk.data.repository

import com.xenia.templekiosk.data.network.model.PersonWithItems
import com.xenia.templekiosk.data.network.service.ApiClient
import com.xenia.templekiosk.data.room.dao.VazhipaduDao
import com.xenia.templekiosk.data.room.entity.Vazhipadu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VazhipaduRepository(private val vazhipaduDao: VazhipaduDao) {

    suspend fun generateOfferingCat(userId: Int, companyId: Int, subTempleId: Int) = withContext(
        Dispatchers.IO
    ) {
        ApiClient.apiService.generateOfferingCat(userId, companyId, subTempleId)
    }

    suspend fun generateOffering(userId: Int, companyId: Int, subTempleId: Int, categoryId: Int) =
        withContext(
            Dispatchers.IO
        ) {
            ApiClient.apiService.generateOffering(userId, companyId, subTempleId, categoryId)
        }

    suspend fun getSelectedVazhipaduItems(): List<Vazhipadu> {
        return vazhipaduDao.getSelectedItems()
    }

    suspend fun getAllVazhipaduItems(): List<Vazhipadu> {
        return vazhipaduDao.getAllCart()
    }

    suspend fun insertCartItem(vazhipadu: Vazhipadu) = withContext(Dispatchers.IO) {
        vazhipaduDao.insertCartItem(vazhipadu)
    }

    suspend fun deleteCartItemByOfferingId(offeringsId: Int) {
        withContext(Dispatchers.IO) {
            vazhipaduDao.deleteCartItemByOfferingId(offeringsId)
        }
    }

    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            vazhipaduDao.truncateTable()
        }
    }

    suspend fun updateNameAndSetCompleted(newName: String) {
        withContext(Dispatchers.IO) {
            vazhipaduDao.updateNameAndSetCompleted(newName)
        }
    }

    suspend fun getDistinctCountOfNameAndStar(): Int {
        return vazhipaduDao.getDistinctCountOfNameAndStar()
    }

    suspend fun updateToIncompleteByNameAndStar(name: String, star: String) {
        vazhipaduDao.updateToIncompleteByNameAndStar(name, star)
    }

    suspend fun getCountForEmptyOrNullNameAndStar(): Int {
        return vazhipaduDao.getCountForEmptyOrNullNameAndStar()
    }

    suspend fun hasIncompleteItems(): Boolean {
        return vazhipaduDao.hasIncompleteItems()
    }

    suspend fun getCartCount(): Int {
        return vazhipaduDao.getCartCount()
    }

    suspend fun getTotalAmount(): Double {
        return vazhipaduDao.getTotalAmount()
    }

    suspend fun getDistinctPersonsWithOfferings(): List<PersonWithItems> {
        val distinctPersons = vazhipaduDao.getDistinctPersons()

        val personWithItemsList = mutableListOf<PersonWithItems>()
        for (person in distinctPersons) {
            val offerings = vazhipaduDao.getVazhipaduByPerson(person.vaName, person.vaStar)

            val personWithItems = PersonWithItems(
                personName = person.vaName,
                personStar = person.vaStar,
                items = offerings
            )
            personWithItemsList.add(personWithItems)
        }

        return personWithItemsList
    }

    suspend fun removeOffering(name: String, star: String, offeringId: Int) {
        vazhipaduDao.deleteOfferingByNameStarAndId(name, star, offeringId)
    }




}