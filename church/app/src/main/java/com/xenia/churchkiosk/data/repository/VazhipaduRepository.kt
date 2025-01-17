package com.xenia.churchkiosk.data.repository


import com.xenia.churchkiosk.data.network.model.PersonWithItems
import com.xenia.churchkiosk.data.network.service.ApiClient
import com.xenia.churchkiosk.data.room.dao.VazhipaduDao
import com.xenia.churchkiosk.data.room.entity.Vazhipadu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VazhipaduRepository(private val vazhipaduDao: VazhipaduDao) {

    suspend fun generateOfferingCat(userId: Int, companyId: Int) = withContext(
        Dispatchers.IO
    ) {
        ApiClient.apiService.generateOfferingCat(userId, companyId)
    }

    suspend fun generateOffering(userId: Int, companyId: Int, categoryId: Int) =
        withContext(
            Dispatchers.IO
        ) {
            ApiClient.apiService.generateOffering(userId, companyId, categoryId)
        }

    suspend fun getSelectedVazhipaduItems(): List<Vazhipadu> {
        return vazhipaduDao.getSelectedItems()
    }

    suspend fun getAllVazhipaduItems(): List<Vazhipadu> {
        return vazhipaduDao.getAllCart()
    }

    suspend fun getLastVazhipaduItems(name: String): List<Vazhipadu> {
        return vazhipaduDao.selectToCompleteByName(name)
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

    suspend fun updateName(newName: String) {
        withContext(Dispatchers.IO) {
            vazhipaduDao.updateName(newName)
        }
    }

    suspend fun updateNameAndSetCompleted(
        newName: String
    ) {
        withContext(Dispatchers.IO) {
            vazhipaduDao.updateNameAndSetCompleted(newName)
        }
    }

    suspend fun getDistinctCountOfName(): Int {
        return vazhipaduDao.getDistinctCountOfName()
    }

    suspend fun getCountForEmptyOrNullName(): Int {
        return vazhipaduDao.getCountForEmptyOrNullName()
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
            val offerings = vazhipaduDao.getVazhipaduByPerson(person.vaName)

            val personWithItems = PersonWithItems(
                personName = person.vaName,
                items = offerings
            )
            personWithItemsList.add(personWithItems)
        }

        return personWithItemsList
    }

    suspend fun removeOffering(name: String, offeringId: Int) {
        vazhipaduDao.deleteOfferingByNameAndId(name, offeringId)
    }




}