package com.example.greenleaf_v100.viewmodel


import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.map
import com.example.greenleaf_v100.DAO.AppDatabase
import com.example.greenleaf_v100.model.CartItem
import com.example.greenleaf_v100.DAO.CartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CarritoViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = CartRepository(
        AppDatabase.getDatabase(application).cartItemDao())


    private val _userId = MutableLiveData<String>()

    val cartItems: LiveData<List<CartItem>> = _userId.switchMap { uid ->
        repo.getAllByUser(uid)
    }

    val total: LiveData<Double> = cartItems.map { list ->
        list.sumOf { it.price }
    }

    fun setUserId(uid: String) {
        _userId.value = uid
    }

    fun addToCart(item: CartItem) = viewModelScope.launch(Dispatchers.IO) {
        repo.add(item)
    }

    fun removeFromCart(item: CartItem) = viewModelScope.launch(Dispatchers.IO) {
        repo.remove(item)
    }
}
