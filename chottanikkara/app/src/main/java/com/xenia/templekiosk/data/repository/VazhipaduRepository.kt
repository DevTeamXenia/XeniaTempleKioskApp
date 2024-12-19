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

    suspend fun getLastVazhipaduItems(name: String, star: String, devathaId: Int): List<Vazhipadu> {
        return vazhipaduDao.selectToCompleteByNameAndStar(name,star,devathaId)
    }

    suspend fun insertCartItem(vazhipadu: Vazhipadu) = withContext(Dispatchers.IO) {
        vazhipaduDao.insertCartItem(vazhipadu)
    }

    suspend fun deleteCartItemByOfferingId(offeringsId: Int, selectedCardId: Int?) {
        withContext(Dispatchers.IO) {
            vazhipaduDao.deleteCartItemByOfferingId(offeringsId,selectedCardId)
        }
    }

    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            vazhipaduDao.truncateTable()
        }
    }

    suspend fun updateName(newName: String) {
        withContext(Dispatchers.IO) {
            vazhipaduDao.updateName(newName)
        }
    }

    suspend fun updateNameAndSetCompleted(
        newName: String,
        englishNakshatra: String?,
        selectedNakshatra: String?
    ) {
        withContext(Dispatchers.IO) {
            vazhipaduDao.updateNameAndSetCompleted(newName,englishNakshatra,selectedNakshatra)
        }
    }

    suspend fun getDistinctCountOfNameAndStar(): Int {
        return vazhipaduDao.getDistinctCountOfNameAndStar()
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
                personStarLa = person.vaStarLa,
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