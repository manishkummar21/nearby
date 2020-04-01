package com.covid.scanning.login

import android.app.Activity
import android.util.Log
import com.covid.scanning.App
import com.covid.scanning.login.model.User
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class LoginInteractor {

    private val disposable = CompositeDisposable()

    interface OnLoginFinishedListener {
        fun onError()
        fun onSuccess()
    }


    fun loginSignup(mobilenumber: String, address: String, listener: OnLoginFinishedListener) {

        disposable.add(
            checkUserExists(mobilenumber)
                .flatMap(object : Function<Pair<Boolean, User>, ObservableSource<User>> {
                    @Throws(Exception::class)
                    override fun apply(t: Pair<Boolean, User>): ObservableSource<User> {
                        if (t.first)
                            return Observable.just(t.second)
                        else
                            return registerUser(mobilenumber, address)
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(object : Function<User, ObservableSource<Boolean>> {
                    @Throws(Exception::class)
                    override fun apply(user: User): ObservableSource<Boolean> {

                        //save user data into db
                        App.getInstance().prefManager.userID = user.user_id
                        App.getInstance().prefManager.userDetails = user
                        App.getInstance().prefManager.isLogged = true

                        return Observable.just(true)
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Boolean>() {

                    override fun onNext(o: Boolean) {
                        listener.onSuccess()

                    }

                    override fun onError(e: Throwable) {
                        listener.onError()
                    }

                    override fun onComplete() {

                    }
                })
        )

    }

    // CheckUserExists
    fun checkUserExists(mobilenumber: String): Observable<Pair<Boolean, User>> {

        return Observable.create<Pair<Boolean, User>> { emitter ->

            App.getInstance().getdbInstance().collection("users").whereEqualTo("mobile_number", mobilenumber)
                .get()
                .addOnSuccessListener { documents ->
                    var user: User? = null
                    if (documents != null && documents.size() > 0) {
                        for (document in documents) {
                            user = document.toObject(User::class.java)
                            break
                        }
                    }
                    emitter.onNext(Pair(user != null, user) as Pair<Boolean, User>)
                    emitter.onComplete()
                }
                .addOnFailureListener { exception ->
                    Log.d("LoginInteractor", "get failed with ", exception)
                    emitter.onError(Throwable(exception.message))
                }

        }
    }

    //Register the user
    fun registerUser(mobilenumber: String, address: String): Observable<User> {

        return Observable.create { emitter ->

            val user_id: String = App.getInstance().getdbInstance().collection("users").document().getId()

            val user = User(user_id, mobilenumber, if (!address.isEmpty()) address else user_id)

            App.getInstance().getdbInstance().collection("users")
                .document(user_id)
                .set(user)
                .addOnSuccessListener { documentReference ->
                    emitter.onNext(user)
                    emitter.onComplete()
                }
                .addOnFailureListener { e ->
                    Log.w("", "Error adding document", e)
                    emitter.onError(Throwable(e.message))
                }

        }

    }


}
