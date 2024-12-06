package com.xenia.templekiosk.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.network.model.CartItem
import com.xenia.templekiosk.ui.adapter.CategoryAdapter
import com.xenia.templekiosk.data.network.model.Category
import com.xenia.templekiosk.data.network.model.Offering
import com.xenia.templekiosk.data.repository.VazhipaduRepository
import com.xenia.templekiosk.databinding.ActivityVazhipaduBinding
import com.xenia.templekiosk.ui.adapter.OfferingAdapter
import com.xenia.templekiosk.ui.dialogue.CustomStarPopupDialogue
import com.xenia.templekiosk.utils.SessionManager
import com.xenia.templekiosk.utils.common.CommonMethod.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class VazhipaduActivity : AppCompatActivity(), CategoryAdapter.OnCategoryClickListener,
    OfferingAdapter.ItemClickListener {

    private lateinit var binding: ActivityVazhipaduBinding
    private val sessionManager: SessionManager by inject()
    private val vazhipaduRepository: VazhipaduRepository by inject()
    private lateinit var itemArray: Array<String>
    private lateinit var categoryAdapter: CategoryAdapter
    private var selectedCategoryId: Int = 1
    private var selectedCardId: Int? = null
    var isDialogVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVazhipaduBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerViews()
        setupListeners()
    }

    override fun onResume() {
        fetchDetails()
        super.onResume()
    }

    override fun onRestart() {
        fetchDetails()
        super.onRestart()
    }

    override fun onBackPressed() {
        sessionManager.clearCart()
        super.onBackPressed()
    }

    private fun setupUI() {
        binding.txtMelkavu?.text = getString(R.string.melkavu_devi)
        binding.txtKeezhkavu?.text = getString(R.string.keezhkavu_devi)
        binding.txtShiva?.text = getString(R.string.shiva)
        binding.txtAyyappa?.text = getString(R.string.ayyappa)

        updateCartCount()
        getDistinctCountOfCartItems()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerViews() {
        val resources = this.resources
        itemArray = resources.getStringArray(R.array.vazhipaduItem)

        categoryAdapter = CategoryAdapter(this, this)

        binding.relOfferCat?.layoutManager = LinearLayoutManager(this)
        binding.relOfferCat?.adapter = categoryAdapter
    }

    private fun setupListeners() {
        binding.editStar?.setOnClickListener {
            val dialog = CustomStarPopupDialogue()
            dialog.onNakshatraSelected = { selectedNakshatra ->
                binding.editStar?.setText(selectedNakshatra)
            }
            dialog.show(supportFragmentManager, "CustomStarPopupDialogue")
        }

        binding.btnPay.setOnClickListener {
            val cartItems = sessionManager.getCartItems()
            val intent = Intent(this, SummaryActivity::class.java)
            intent.putExtra("cartItems", Gson().toJson(cartItems))
            startActivity(intent)
        }

        binding.linHome?.setOnClickListener {
            sessionManager.clearCart()
            startActivity(Intent(applicationContext, LanguageActivity::class.java))
            finish()
        }

        val defaultBackground = R.drawable.bg_grey_card
        val selectedBackground = R.drawable.bg_card

        val cards = mapOf(
            R.id.card_melkavu to 1,
            R.id.card_keezhkavu to 2,
            R.id.card_shiva to 3,
            R.id.card_ayyappa to 4
        )

        val cardViews = mapOf(
            R.id.card_melkavu to binding.cardMelkavu,
            R.id.card_keezhkavu to binding.cardKeezhkavu,
            R.id.card_shiva to binding.cardShiva,
            R.id.card_ayyappa to binding.cardAyyappa
        )

        val selectedCard = R.id.card_melkavu
        cardViews.forEach { (id, card) ->
            if (id == selectedCard) {
                card?.setBackgroundResource(selectedBackground)
            } else {
                card?.setBackgroundResource(defaultBackground)
            }
        }

        selectedCardId = cards[selectedCard]


        cardViews.forEach { (id, card) ->
            card?.setOnClickListener {
                cardViews.entries.forEach { (_, value) ->
                    value?.setBackgroundResource(defaultBackground)
                }
                card.setBackgroundResource(selectedBackground)
                selectedCardId = cards[id]

                fetchDetails()

            }
        }


        binding.editName?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.addMore?.setOnClickListener {
            val userName = binding.editName?.text.toString()
            val userStar = binding.editStar?.text.toString()

            if (userName.isNotEmpty() && userStar.isNotEmpty()) {
                val updatedCartItems = sessionManager.getCartItems()
                updatedCartItems.forEach {
                    it.personName = userName
                    it.personStar = userStar
                }
                updatedCartItems.forEach {
                    println("Updated Cart Item: ${it.offeringName}, Name: ${it.personName}, Star: ${it.personStar}")
                }
                sessionManager.saveCartItems(updatedCartItems)
                updateCartCount()
                getDistinctCountOfCartItems()
                updateButtonState()

            } else {
                if (userName.isEmpty()) {
                    binding.editName?.requestFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.editName, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }


    }

    private fun fetchDetails() {
        getOfferingCategory()
    }

    private fun getOfferingCategory() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    vazhipaduRepository.generateOfferingCat(
                        sessionManager.getUserId(),
                        sessionManager.getCompanyId(),
                        selectedCardId!!
                    )
                }

                when {
                    response.status.equals("success", ignoreCase = true) -> {
                        val categories = response.data

                        if (categories.isNullOrEmpty()) {
                            showSnackbar(binding.root, "No categories found.")
                        } else {
                            val activeCategories = categories.filter { it.categoryActive }

                            if (activeCategories.isNotEmpty()) {
                                categoryAdapter.updateCategories(activeCategories)
                                fetchOfferingsForCategory(selectedCategoryId)
                            } else {
                                showSnackbar(binding.root, "No active categories found.")
                            }
                        }
                    }

                    else -> {
                        showSnackbar(
                            binding.root,
                            response.message ?: "Failed to fetch categories."
                        )
                    }
                }
            } catch (e: Exception) {
                showSnackbar(binding.root, "Error: ${e.localizedMessage}")
            }
        }
    }

    private fun fetchOfferingsForCategory(categoryId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    vazhipaduRepository.generateOffering(
                        sessionManager.getUserId(),
                        sessionManager.getCompanyId(),
                        selectedCardId!!,
                        categoryId
                    )
                }

                when {
                    response.status.equals("success", ignoreCase = true) -> {
                        val items = response.data
                        if (items.isNullOrEmpty()) {
                            showSnackbar(binding.root, "No items found for this category.")
                        } else {
                            val cartItems = sessionManager.getCartItems()
                            val selectedItems = items.filter { item ->
                                cartItems.any { cartItem -> cartItem.offeringId == item.offeringsId.toString() }
                            }

                            binding.relOffers?.layoutManager =
                                GridLayoutManager(this@VazhipaduActivity, 3)
                            binding.relOffers?.adapter =
                                OfferingAdapter(items, this@VazhipaduActivity, selectedItems)
                        }
                    }

                    else -> {
                        showSnackbar(binding.root, response.message ?: "Failed to fetch items.")
                    }
                }
            } catch (e: Exception) {
                showSnackbar(binding.root, "Error: ${e.localizedMessage}")
            }
        }
    }

    override fun onCategoryClick(category: Category) {
        selectedCategoryId = category.categoryId
        fetchOfferingsForCategory(selectedCategoryId)
    }


    override fun onItemAdded(item: Offering) {
        if (binding.editStar?.text.isNullOrEmpty()) {
            if (!isDialogVisible) {
                isDialogVisible = true
                val dialog = CustomStarPopupDialogue()

                dialog.onNakshatraSelected = { selectedNakshatra ->
                    binding.editStar?.setText(selectedNakshatra)
                    val cartItem = CartItem(
                        categoryId = item.offeringsCategoryId.toString(),
                        offeringId = item.offeringsId.toString(),
                        subTempleId = selectedCardId.toString(),
                        offeringName = item.offeringsName,
                        amount = item.offeringsAmount
                    )
                    sessionManager.addToCart(cartItem)
                    updateCartCount()
                }

                dialog.show(supportFragmentManager, "CustomStarPopupDialogue")
            }
        } else {
            val cartItem = CartItem(
                categoryId = item.offeringsCategoryId.toString(),
                offeringId = item.offeringsId.toString(),
                subTempleId = "1",
                offeringName = item.offeringsName,
                amount = item.offeringsAmount
            )

            sessionManager.addToCart(cartItem)
            updateCartCount()
            getDistinctCountOfCartItems()
            updateButtonState()
        }
    }

    override fun onItemRemoved(item: Offering) {
        if (binding.editStar?.text.isNullOrEmpty()) {
            if (!isDialogVisible) {
                isDialogVisible = true
                val dialog = CustomStarPopupDialogue()

                dialog.onNakshatraSelected = { selectedNakshatra ->
                    binding.editStar?.setText(selectedNakshatra)
                    val cartItem = CartItem(
                        categoryId = item.offeringsCategoryId.toString(),
                        offeringId = item.offeringsId.toString(),
                        subTempleId = "1",
                        offeringName = item.offeringsName,
                        amount = item.offeringsAmount
                    )
                    sessionManager.removeFromCart(cartItem)
                    updateCartCount()
                    getDistinctCountOfCartItems()
                    updateButtonState()
                }

                dialog.show(supportFragmentManager, "CustomStarPopupDialogue")
            }
        } else {
            val cartItem = CartItem(
                categoryId = item.offeringsCategoryId.toString(),
                offeringId = item.offeringsId.toString(),
                subTempleId = "1",
                offeringName = item.offeringsName,
                amount = item.offeringsAmount
            )

            sessionManager.removeFromCart(cartItem)
            updateCartCount()
        }
    }


    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateCartCount() {
        val cartCount = sessionManager.getCartCount()
        binding.cartCartCount?.text = cartCount.toString()

        val totalAmount = calculateTotalAmount()
        val btnPay: MaterialButton = findViewById(R.id.btn_pay)
        btnPay.text = "Pay: ₹${String.format("%.2f", totalAmount)}"
    }

    private fun calculateTotalAmount(): Double {
        val cartItems = sessionManager.getCartItems()
        var totalAmount = 0.0
        for (item in cartItems) {
            totalAmount += item.amount
        }

        return totalAmount
    }

    @SuppressLint("SetTextI18n")
    private fun getDistinctCountOfCartItems() {
        val cartItems = sessionManager.getCartItems()
        val distinctCartItems = cartItems.distinctBy { Pair(it.personName, it.personStar) }
        binding.cartPersonCartCount?.text = distinctCartItems.size.toString()

    }


    private fun updateButtonState() {
        val userName = binding.editName?.text.toString()
        val userStar = binding.editStar?.text.toString()


        if (userName.isNotEmpty() && userStar.isNotEmpty() && sessionManager.getCartCount() > 0) {
            val updatedCartItems = sessionManager.getCartItems()
            updatedCartItems.forEach {
                it.personName = userName
                it.personStar = userStar
            }
            updatedCartItems.forEach {
                println("Updated Cart Item: ${it.offeringName}, Name: ${it.personName}, Star: ${it.personStar}")
            }
            sessionManager.saveCartItems(updatedCartItems)
            binding.btnPay.isEnabled = true
            binding.btnPay.setBackgroundColor(
                ContextCompat.getColor(this, R.color.primaryColor)
            )
        } else {
            binding.btnPay.isEnabled = false
            binding.btnPay.setBackgroundColor(
                ContextCompat.getColor(this, R.color.light_grey)
            )
        }
    }

}
