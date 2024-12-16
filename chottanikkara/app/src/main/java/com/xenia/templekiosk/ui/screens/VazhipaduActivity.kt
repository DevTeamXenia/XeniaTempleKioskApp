package com.xenia.templekiosk.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.xenia.templekiosk.R
import com.xenia.templekiosk.ui.adapter.CategoryAdapter
import com.xenia.templekiosk.data.network.model.Category
import com.xenia.templekiosk.data.network.model.Offering
import com.xenia.templekiosk.data.repository.VazhipaduRepository
import com.xenia.templekiosk.data.room.entity.Vazhipadu
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
    private val sharedPreferences: SharedPreferences by inject()
    private val vazhipaduRepository: VazhipaduRepository by inject()
    private lateinit var itemArray: Array<String>
    private lateinit var categoryAdapter: CategoryAdapter
    private var selectedCategoryId: Int = 1
    private var selectedCardId: Int? = null
    private var selectedCardName: String? = null
    private var englishNakshatra: String? = null
    private var selectedNakshatra: String? = null
    private var selectedUserName: String? = null
    private var isCardClick: Boolean = false
    var isDialogVisible = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVazhipaduBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedUserName = intent.getStringExtra("USER_NAME")
        englishNakshatra = intent.getStringExtra("STAR")
        selectedNakshatra = intent.getStringExtra("SELECTED_STAR")

        onBackPressedDispatcher.addCallback(this) {
            lifecycleScope.launch {
                vazhipaduRepository.clearAllData()
                finish()
            }
        }

        setupUI()
        setupRecyclerViews()
        setupListeners()
    }

    override fun onResume() {
        fetchDetails()
        updateCartCount()
        updateButtonState()
        super.onResume()
    }

    override fun onRestart() {
        fetchDetails()
        super.onRestart()
    }


    private fun setupUI() {
        binding.txtMelkavu?.text = getString(R.string.melkavu_devi)
        binding.txtKeezhkavu?.text = getString(R.string.keezhkavu_devi)
        binding.txtShiva?.text = getString(R.string.shiva)
        binding.txtAyyappa?.text = getString(R.string.ayyappa)
        binding.txtPeople?.text = getString(R.string.more_people)
        updateCartCount()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerViews() {
        val resources = this.resources
        itemArray = resources.getStringArray(R.array.vazhipaduItem)

        categoryAdapter = CategoryAdapter(this, sharedPreferences, this)

        binding.relOfferCat?.layoutManager = LinearLayoutManager(this)
        binding.relOfferCat?.adapter = categoryAdapter
    }

    private fun setupListeners() {
        binding.editStar?.setOnClickListener {
            val dialog = CustomStarPopupDialogue()
            dialog.onNakshatraSelected = { selectedLaNakshatra,selectedEnglishNakshatra  ->
                binding.editStar?.setText(selectedLaNakshatra)
                englishNakshatra = selectedEnglishNakshatra
                selectedNakshatra = selectedLaNakshatra
            }
            dialog.show(supportFragmentManager, "CustomStarPopupDialogue")
        }

        binding.btnPay.setOnClickListener {
            val userName = binding.editName?.text.toString()
            val star = binding.editStar?.text.toString()
            lifecycleScope.launch {
                val cartCount = vazhipaduRepository.getCartCount()
                val countForEmptyOrNullNameAndStar = vazhipaduRepository.getCountForEmptyOrNullNameAndStar()
                val hasIncompleteItems = vazhipaduRepository.hasIncompleteItems()

                if (cartCount > 0) {
                    if (hasIncompleteItems && countForEmptyOrNullNameAndStar > 0) {
                        if (userName.isEmpty()) {
                            showSnackbar(binding.root, "Please enter your name")
                        } else if (star.isEmpty()) {
                            showSnackbar(binding.root, "Please select your star")
                        } else {
                            completeAndNavigate(userName)
                        }
                    } else {
                        completeAndNavigate(userName)
                    }
                } else {
                    if (userName.isEmpty()) {
                        showSnackbar(binding.root, "Please enter your name")
                    } else if (star.isEmpty()) {
                        showSnackbar(binding.root, "Please select your star")
                    } else {
                        completeAndNavigate(userName)
                    }
                }
            }
        }


        binding.linCart?.setOnClickListener {
            val userName = binding.editName?.text.toString()
            val star = binding.editStar?.text.toString()
            lifecycleScope.launch {
                val cartCount = vazhipaduRepository.getCartCount()
                val countForEmptyOrNullNameAndStar = vazhipaduRepository.getCountForEmptyOrNullNameAndStar()
                val hasIncompleteItems = vazhipaduRepository.hasIncompleteItems()

                if (cartCount > 0) {
                    if (hasIncompleteItems && countForEmptyOrNullNameAndStar > 0) {
                        if (userName.isEmpty()) {
                            showSnackbar(binding.root, "Please enter your name")
                        } else if (star.isEmpty()) {
                            showSnackbar(binding.root, "Please select your star")
                        } else {
                            completeAndNavigate(userName)
                        }
                    } else {
                        completeAndNavigate(userName)
                    }
                } else {
                    if (userName.isEmpty()) {
                        showSnackbar(binding.root, "Please enter your name")
                    } else if (star.isEmpty()) {
                        showSnackbar(binding.root, "Please select your star")
                    } else {
                        completeAndNavigate(userName)
                    }
                }
            }
        }

        binding.linPersonCart?.setOnClickListener {
            val userName = binding.editName?.text.toString()
            val star = binding.editStar?.text.toString()
            lifecycleScope.launch {
                val cartCount = vazhipaduRepository.getCartCount()
                val countForEmptyOrNullNameAndStar = vazhipaduRepository.getCountForEmptyOrNullNameAndStar()
                val hasIncompleteItems = vazhipaduRepository.hasIncompleteItems()

                if (cartCount > 0) {
                    if (hasIncompleteItems && countForEmptyOrNullNameAndStar > 0) {
                        if (userName.isEmpty()) {
                            showSnackbar(binding.root, "Please enter your name")
                        } else if (star.isEmpty()) {
                            showSnackbar(binding.root, "Please select your star")
                        } else {
                            completeAndNavigate(userName)
                        }
                    } else {
                        completeAndNavigate(userName)
                    }
                } else {
                    if (userName.isEmpty()) {
                        showSnackbar(binding.root, "Please enter your name")
                    } else if (star.isEmpty()) {
                        showSnackbar(binding.root, "Please select your star")
                    } else {
                        completeAndNavigate(userName)
                    }
                }
            }
        }


        binding.linHome?.setOnClickListener {
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

        val cardNames = mapOf(
            R.id.card_melkavu to  getString(R.string.melkavu_devi),
            R.id.card_keezhkavu to getString(R.string.keezhkavu_devi),
            R.id.card_shiva to getString(R.string.shiva),
            R.id.card_ayyappa to getString(R.string.ayyappa)
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
        selectedCardName = cardNames[selectedCard]

        cardViews.forEach { (id, card) ->
            card?.setOnClickListener {
                cardViews.entries.forEach { (_, value) ->
                    value?.setBackgroundResource(defaultBackground)
                }

                card.setBackgroundResource(selectedBackground)
                selectedCardId = cards[id]
                selectedCardName = cardNames[id]
                isCardClick = true
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
                lifecycleScope.launch {
                    vazhipaduRepository.updateNameAndSetCompleted(userName,selectedCardId,selectedCardName)
                    updateButtonState()
                        binding.editName?.setText("")
                        binding.editStar?.setText("")
                        selectedUserName = ""
                        selectedNakshatra = ""
                        englishNakshatra = ""
                    isCardClick = false
                    fetchDetails()
                }

            } else {
                if (userName.isEmpty()) {
                    binding.editName?.requestFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.editName, InputMethodManager.SHOW_IMPLICIT)

                }
            }
        }

    }


    private fun completeAndNavigate(userName: String) {
        lifecycleScope.launch {
            vazhipaduRepository.updateNameAndSetCompleted(
                userName,
                selectedCardId,
                selectedCardName
            )
            val intent = Intent(this@VazhipaduActivity, SummaryActivity::class.java).apply {
                putExtra("USER_NAME", userName)
                putExtra("STAR", englishNakshatra)
                putExtra("SELECTED_STAR", selectedNakshatra)
            }
            startActivity(intent)
            finish()
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
                            val selectedItems: List<Vazhipadu> = if (selectedUserName.isNullOrEmpty() && englishNakshatra.isNullOrEmpty()) {
                                vazhipaduRepository.getSelectedVazhipaduItems()
                            } else {
                                if (!isCardClick) {
                                    binding.editName?.text = selectedUserName?.let { Editable.Factory.getInstance().newEditable(it) }
                                    binding.editStar?.text = selectedNakshatra?.let { Editable.Factory.getInstance().newEditable(it) }
                                }
                                vazhipaduRepository.getLastVazhipaduItems(selectedUserName ?: "", englishNakshatra ?: "")
                            }

                            val filteredItems = items.filter { item ->
                                selectedItems.any { selectedItem ->
                                    selectedItem.vaOfferingsId == item.offeringsId
                                }
                            }

                            binding.relOffers?.layoutManager =
                                GridLayoutManager(this@VazhipaduActivity, 3)
                            binding.relOffers?.adapter =
                                OfferingAdapter(
                                    items,
                                    sharedPreferences,
                                    this@VazhipaduActivity,
                                    filteredItems
                                )
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
                showNakshatraDialog(item)
            }
        } else {
            addToCart(item, englishNakshatra!!, selectedNakshatra!!)
        }
    }


    override fun onItemRemoved(item: Offering) {
        if (binding.editStar?.text.isNullOrEmpty()) {
            if (!isDialogVisible) {
                isDialogVisible = true
                showNakshatraDialog(item)
            }
        } else {
            deleteToCart(item)
        }
    }


    private fun showNakshatraDialog(item: Offering) {
        val dialog = CustomStarPopupDialogue()

        dialog.onNakshatraSelected = { selectedLaNakshatra,selectedEnglishNakshatra  ->
            binding.editStar?.setText(selectedLaNakshatra)
            englishNakshatra = selectedEnglishNakshatra
            selectedNakshatra = selectedLaNakshatra
            addToCart(item,selectedEnglishNakshatra,selectedLaNakshatra)
        }

        dialog.show(supportFragmentManager, "CustomStarPopupDialogue")
    }

    private fun addToCart(
        item: Offering,
        selectedEnglishNakshatra: String,
        selectedNakshatra: String
    ) {
        val cartItem = Vazhipadu(
            vaName = binding.editName?.text.toString(),
            vaStar = selectedEnglishNakshatra,
            vaStarLa = selectedNakshatra,
            vaPhoneNumber = "",
            vaOfferingsId = item.offeringsId,
            vaOfferingsName = item.offeringsName,
            vaOfferingsNameMa = item.offeringsNameMa,
            vaOfferingsNameTa = item.offeringsNameTa,
            vaOfferingsNameKa = item.offeringsNameKa,
            vaOfferingsNameTe = item.offeringsNameTe,
            vaOfferingsNameHi = item.offeringsNameHi,
            vaOfferingsAmount = item.offeringsAmount,
            vaSubTempleId = selectedCardId!!,
            vaSubTempleName = selectedCardName!!,
            vaIsCompleted = false
        )

        lifecycleScope.launch {
            vazhipaduRepository.insertCartItem(cartItem)
            updateCartCount()
            updateButtonState()
        }
    }

    private fun deleteToCart(item: Offering) {
        lifecycleScope.launch {
            vazhipaduRepository.deleteCartItemByOfferingId(item.offeringsId)
            updateCartCount()
            updateButtonState()
        }
    }


    @SuppressLint("DefaultLocale", "SetTextI18n", "StringFormatInvalid")
    private fun updateCartCount() {
        lifecycleScope.launch {
            val cartCount = vazhipaduRepository.getCartCount()
            binding.cartCartCount?.text = cartCount.toString()

            val cartPersonCount = vazhipaduRepository.getDistinctCountOfNameAndStar()
            binding.cartPersonCartCount?.text = cartPersonCount.toString()

            val totalAmount = vazhipaduRepository.getTotalAmount()
            val formattedAmount = String.format("%.2f", totalAmount)

            val btnPay: MaterialButton = findViewById(R.id.btn_pay)
            btnPay.text = getString(R.string.pay_vazhipadu, formattedAmount)

        }
    }

    private fun updateButtonState() {
        val userName = binding.editName?.text.toString()
        val userStar = binding.editStar?.text.toString()
        lifecycleScope.launch {
            val cartCount = vazhipaduRepository.getCartCount()
            val countForEmptyOrNullNameAndStar =
                vazhipaduRepository.getCountForEmptyOrNullNameAndStar()

            if (cartCount > 0 && countForEmptyOrNullNameAndStar <= 0) {
                binding.btnPay.setBackgroundColor(
                    ContextCompat.getColor(this@VazhipaduActivity, R.color.primaryColor)
                )
            } else if (userName.isNotEmpty() && userStar.isNotEmpty() && cartCount > 0) {
                binding.btnPay.setBackgroundColor(
                    ContextCompat.getColor(this@VazhipaduActivity, R.color.primaryColor)
                )
            } else {
                binding.btnPay.setBackgroundColor(
                    ContextCompat.getColor(this@VazhipaduActivity, R.color.light_grey)
                )
            }
        }
    }


}
