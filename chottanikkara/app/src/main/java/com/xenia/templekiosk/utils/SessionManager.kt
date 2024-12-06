package com.xenia.templekiosk.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.xenia.templekiosk.data.network.model.CartItem
import com.xenia.templekiosk.data.network.model.Company
import com.xenia.templekiosk.data.network.model.Offering

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    private val cartItems = mutableListOf<CartItem>()

    companion object {
        private const val PREF_NAME = "UserSession"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_COMPANY_ID = "companyId"
        private const val KEY_COMPANY_OBJECT = "company"
        private const val KEY_CART_ITEMS = "cartItems"
    }

    fun saveUserDetails(userId: Int, userName: String, companyId: Int) {
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, userName)
        editor.putInt(KEY_COMPANY_ID, companyId)
        editor.apply()
    }


    fun saveCompanyDetails(company: Company) {
        val gson = Gson()
        val companyJson = gson.toJson(company)
        editor.putString(KEY_COMPANY_OBJECT, companyJson)
        editor.apply()
    }


    fun getCompanyDetails(): Company? {
        val gson = Gson()
        val companyJson = sharedPreferences.getString(KEY_COMPANY_OBJECT, null)
        return if (companyJson != null) {
            gson.fromJson(companyJson, Company::class.java)
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return getUserName() != null
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, 0)
    }

    private fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    fun getCompanyId(): Int {
        return sharedPreferences.getInt(KEY_COMPANY_ID, 0)
    }

    fun clearSession() {
        editor.clear()
        editor.apply()
    }

    fun addToCart(cartItem: CartItem) {
        if (!cartItems.contains(cartItem)) {
            cartItems.add(cartItem)
        }
    }


    fun removeFromCart(cartItem: CartItem) {
        cartItems.remove(cartItem)
    }


    fun saveCartItems(updatedCartItems: List<CartItem>) {
        val gson = Gson()
        val cartItemsJson = gson.toJson(updatedCartItems)
        editor.putString(KEY_CART_ITEMS, cartItemsJson)
        editor.apply()
    }

    fun removeCartItem(cartItem: CartItem) {
        // Get the current list of cart items from SharedPreferences
        val cartItemsJson = sharedPreferences.getString(KEY_CART_ITEMS, "[]")
        val cartItems = Gson().fromJson(cartItemsJson, Array<CartItem>::class.java).toMutableList()

        // Remove the item with the matching offeringId
        cartItems.removeIf { it.offeringId == cartItem.offeringId }

        // Save the updated list of cart items back to SharedPreferences
        editor.putString(KEY_CART_ITEMS, Gson().toJson(cartItems))
        editor.apply()

        // Update the in-memory cartItems list
        this.cartItems.clear()
        this.cartItems.addAll(cartItems)
    }



    fun getCartCount(): Int {
        return cartItems.size
    }


    fun getCartItems(): List<CartItem> {
        return cartItems
    }

    fun clearCart() {
        cartItems.clear()
        editor.remove(KEY_CART_ITEMS)
        editor.apply()

    }
}
