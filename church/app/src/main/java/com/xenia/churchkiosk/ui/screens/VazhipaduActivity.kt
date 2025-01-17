package com.xenia.churchkiosk.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.xenia.churchkiosk.R
import com.xenia.churchkiosk.data.repository.VazhipaduRepository
import com.xenia.churchkiosk.data.room.entity.Vazhipadu
import com.xenia.churchkiosk.databinding.ActivityVazhipaduBinding
import com.xenia.churchkiosk.ui.adapter.CategoryAdapter
import com.xenia.churchkiosk.ui.adapter.OfferingAdapter
import com.xenia.churchkiosk.ui.dialogue.CustomInactivityDialog
import com.xenia.churchkiosk.ui.dialogue.CustomQRPopupDialogue
import com.xenia.churchkiosk.utils.InactivityHandler
import com.xenia.churchkiosk.utils.SessionManager
import com.xenia.churchkiosk.utils.common.CommonMethod.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject


class VazhipaduActivity : AppCompatActivity(), CategoryAdapter.OnCategoryClickListener,
    OfferingAdapter.ItemClickListener, CustomInactivityDialog.InactivityCallback,
    com.xenia.churchkiosk.data.listeners.InactivityHandlerActivity {

    private lateinit var binding: ActivityVazhipaduBinding
    private val sessionManager: SessionManager by inject()
    private val sharedPreferences: SharedPreferences by inject()
    private val vazhipaduRepository: VazhipaduRepository by inject()
    private val customQRPopupDialog: CustomQRPopupDialogue by inject()
    private lateinit var itemArray: Array<String>
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var offeringAdapter: OfferingAdapter
    private lateinit var inactivityHandler: InactivityHandler
    private lateinit var inactivityDialog: CustomInactivityDialog
    private var selectedUserName: String? = null
    private var selectedCategoryId: Int = 8
    private var spanCount: Int? = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVazhipaduBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedUserName = intent.getStringExtra("USER_NAME")

        onBackPressedDispatcher.addCallback(this) {
            lifecycleScope.launch {
                vazhipaduRepository.clearAllData()
                finish()
            }
        }
        inactivityDialog = CustomInactivityDialog(this)
        inactivityHandler =
            InactivityHandler(this, supportFragmentManager, inactivityDialog, customQRPopupDialog)

        setupUI()
        setupRecyclerViews()
        setupListeners()
    }

    override fun resetInactivityTimer() {
        inactivityHandler.resetTimer()
    }

    override fun onResume() {
        fetchDetails()
        updateCartCount()
        updateButtonState()
        super.onResume()
        inactivityHandler.resumeInactivityCheck()
    }

    override fun onRestart() {
        fetchDetails()
        super.onRestart()
    }

    override fun onPause() {
        super.onPause()
        inactivityHandler.pauseInactivityCheck()
    }

    override fun onDestroy() {
        super.onDestroy()
        inactivityHandler.cleanup()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        inactivityHandler.resetTimer()
        return super.dispatchTouchEvent(ev)
    }


    private fun setupUI() {
        binding.txtPeople?.text = getString(R.string.more_people)
        updateCartCount()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerViews() {
        val resources = this.resources
        itemArray = resources.getStringArray(R.array.vazhipaduItem)

        categoryAdapter = CategoryAdapter(
            this,
            sharedPreferences,
            this
        )

        binding.relOfferCat?.layoutManager = LinearLayoutManager(this)
        binding.relOfferCat?.adapter = categoryAdapter
    }

    private fun setupListeners() {

        binding.btnPay.setOnClickListener {
            val userName = binding.editName?.text.toString()
            lifecycleScope.launch {
                val cartCount = vazhipaduRepository.getCartCount()
                val countForEmptyOrNullNameAndStar =
                    vazhipaduRepository.getCountForEmptyOrNullName()
                val hasIncompleteItems = vazhipaduRepository.hasIncompleteItems()

                if (cartCount > 0) {
                    if (hasIncompleteItems && countForEmptyOrNullNameAndStar > 0) {
                        if (userName.isEmpty()) {
                            binding.editName?.requestFocus()
                            showKeyboard(binding.editName)
                            showSnackbar(binding.root, "Please enter your name")
                        } else {
                            completeAndNavigate(userName)
                        }
                    } else {
                        completeAndNavigate(userName)
                    }
                } else {
                    if (userName.isEmpty()) {
                        binding.editName?.requestFocus()
                        showKeyboard(binding.editName)
                        showSnackbar(binding.root, "Please enter your name")
                    } else {
                        completeAndNavigate(userName)
                    }
                }
            }
        }


        binding.linCart?.setOnClickListener {
            val userName = binding.editName?.text.toString()
            lifecycleScope.launch {
                val cartCount = vazhipaduRepository.getCartCount()
                val countForEmptyOrNullNameAndStar =
                    vazhipaduRepository.getCountForEmptyOrNullName()
                val hasIncompleteItems = vazhipaduRepository.hasIncompleteItems()

                if (cartCount > 0) {
                    if (hasIncompleteItems && countForEmptyOrNullNameAndStar > 0) {
                        if (userName.isEmpty()) {
                            showKeyboard(binding.editName)
                            showSnackbar(binding.root, "Please enter your name")
                        }else {
                            completeAndNavigate(userName)
                        }
                    } else {
                        completeAndNavigate(userName)
                    }
                } else {
                    if (userName.isEmpty()) {
                        showSnackbar(binding.root, "Please enter your name")
                    }else {
                        completeAndNavigate(userName)
                    }
                }
            }
        }

        binding.linPersonCart?.setOnClickListener {
            val userName = binding.editName?.text.toString()
            lifecycleScope.launch {
                val cartCount = vazhipaduRepository.getCartCount()
                val countForEmptyOrNullNameAndStar =
                    vazhipaduRepository.getCountForEmptyOrNullName()
                val hasIncompleteItems = vazhipaduRepository.hasIncompleteItems()

                if (cartCount > 0) {
                    if (hasIncompleteItems && countForEmptyOrNullNameAndStar > 0) {
                        if (userName.isEmpty()) {
                            showKeyboard(binding.editName)
                            showSnackbar(binding.root, "Please enter your name")
                        } else {
                            completeAndNavigate(userName)
                        }
                    } else {
                        completeAndNavigate(userName)
                    }
                } else {
                    if (userName.isEmpty()) {
                        showSnackbar(binding.root, "Please enter your name")
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


        binding.editName?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.addMore?.setOnClickListener {
            val userName = binding.editName?.text.toString()
            if (userName.isNotEmpty()) {
                lifecycleScope.launch {
                    vazhipaduRepository.updateNameAndSetCompleted(
                        userName
                    )
                    updateButtonState()
                    binding.editName?.setText("")
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
            vazhipaduRepository.updateNameAndSetCompleted(userName)
            val intent = Intent(this@VazhipaduActivity, SummaryActivity::class.java).apply {
                putExtra("USER_NAME", userName)
            }
            startActivity(intent)
            finish()
        }

    }

    private fun fetchDetails() {
        if (sessionManager.getCompanyDetails()?.isCategoryEnable == true) {
            binding.linOfferCat?.visibility = View.VISIBLE
            getOfferingCategory()
        } else {
            spanCount = 4
            binding.linOfferCat?.visibility = View.GONE
            fetchOfferingsForCategory(selectedCategoryId)
        }
    }

    private fun getOfferingCategory() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    vazhipaduRepository.generateOfferingCat(
                        sessionManager.getUserId(),
                        sessionManager.getCompanyId()
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
                                fetchOfferingsForCategory(activeCategories[0].categoryId)
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
                        categoryId
                    )
                }
                when {
                    response.status.equals("success", ignoreCase = true) -> {
                        val items = response.data
                        if (items.isNullOrEmpty()) {
                            showSnackbar(binding.root, "No items found for this category.")
                        } else {

                            val selectedItems: List<Vazhipadu> =
                                if (selectedUserName.isNullOrEmpty()) {
                                    vazhipaduRepository.getSelectedVazhipaduItems()
                                } else {
                                    if(selectedUserName.isNullOrEmpty()){
                                        vazhipaduRepository.getLastVazhipaduItems(
                                            binding.editName?.text.toString()
                                        )
                                    }else{
                                        binding.editName?.text = selectedUserName?.let {
                                            Editable.Factory.getInstance().newEditable(it)
                                        }
                                        vazhipaduRepository.getLastVazhipaduItems(
                                            selectedUserName ?: ""
                                        )
                                    }

                                }


                            val filteredItems = items.filter { item ->
                                selectedItems.any { selectedItem ->
                                    selectedItem.vaOfferingsId == item.offeringsId
                                }
                            }
                            if (!::offeringAdapter.isInitialized) {
                                offeringAdapter = OfferingAdapter(
                                    items,
                                    sharedPreferences,
                                    this@VazhipaduActivity,
                                    filteredItems
                                )
                                binding.relOffers?.layoutManager =
                                    GridLayoutManager(this@VazhipaduActivity, spanCount!!)
                                binding.relOffers?.adapter = offeringAdapter
                            } else {
                                offeringAdapter.updateData(items, filteredItems)
                            }
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


    override fun onCategoryClick(category: com.xenia.churchkiosk.data.network.model.Category) {
        selectedCategoryId = category.categoryId
        fetchOfferingsForCategory(selectedCategoryId)
    }


    override fun onItemAdded(
        item: com.xenia.churchkiosk.data.network.model.Offering,
        selectedItemIds: MutableSet<String>
    ) {
        addToCart(item)
    }


    override fun onItemRemoved(
        item: com.xenia.churchkiosk.data.network.model.Offering,
        selectedItemIds: MutableSet<String>
    ) {
        deleteToCart(item)
    }


    private fun addToCart(
        item: com.xenia.churchkiosk.data.network.model.Offering
    ) {
        val cartItem = Vazhipadu(
            vaName = binding.editName?.text.toString(),
            vaPhoneNumber = "",
            vaOfferingsId = item.offeringsId,
            vaOfferingsName = item.offeringsName,
            vaOfferingsNameMa = item.offeringsNameMa,
            vaOfferingsNameTa = item.offeringsNameTa,
            vaOfferingsNameKa = item.offeringsNameKa,
            vaOfferingsNameTe = item.offeringsNameTe,
            vaOfferingsNameHi = item.offeringsNameHi,
            vaOfferingsAmount = item.offeringsAmount,
            vaCategoryName = item.categoryName,
            vaCategoryNameHi = item.categoryNameHi,
            vaCategoryNameMa = item.categoryNameMa,
            vaCategoryNameKa = item.categoryNameKa,
            vaCategoryNameTa = item.categoryNameTa,
            vaCategoryNameTe = item.categoryNameTe,
            vaIsCompleted = false
        )

        lifecycleScope.launch {
            vazhipaduRepository.insertCartItem(cartItem)
            updateCartCount()
            updateButtonState()
            offeringAdapter.updateBackgroundForItem(item.offeringsId.toString())
        }
    }

    private fun deleteToCart(item: com.xenia.churchkiosk.data.network.model.Offering) {
        lifecycleScope.launch {
            vazhipaduRepository.deleteCartItemByOfferingId(item.offeringsId)
            updateCartCount()
            updateButtonState()
            offeringAdapter.updateBackgroundForItem(item.offeringsId.toString())
        }
    }


    @SuppressLint("DefaultLocale", "SetTextI18n", "StringFormatInvalid")
    private fun updateCartCount() {
        lifecycleScope.launch {
            val cartCount = vazhipaduRepository.getCartCount()
            binding.cartCartCount?.text = cartCount.toString()

            val cartPersonCount = vazhipaduRepository.getDistinctCountOfName()
            binding.cartPersonCartCount?.text = cartPersonCount.toString()

            val totalAmount = vazhipaduRepository.getTotalAmount()
            val formattedAmount = String.format("%.2f", totalAmount)

            val btnPay: MaterialButton = findViewById(R.id.btn_pay)
            btnPay.text = getString(R.string.pay_vazhipadu, formattedAmount)

        }
    }

    private fun updateButtonState() {
        val userName = binding.editName?.text.toString()
        lifecycleScope.launch {
            val cartCount = vazhipaduRepository.getCartCount()
            val countForEmptyOrNullName =
                vazhipaduRepository.getCountForEmptyOrNullName()

            if (cartCount > 0 && countForEmptyOrNullName <= 0) {
                binding.btnPay.setBackgroundColor(
                    ContextCompat.getColor(this@VazhipaduActivity, R.color.primaryColor)
                )
            } else if (userName.isNotEmpty() && cartCount > 0) {
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

    private fun showKeyboard(editText: EditText?) {
        editText?.let {
            val inputMethodManager =
                it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            it.requestFocus()
            inputMethodManager.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
        }
    }

}
