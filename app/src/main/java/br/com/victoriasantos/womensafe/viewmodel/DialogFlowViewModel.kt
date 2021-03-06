package br.com.victoriasantos.womensafe.viewmodel

import android.app.Application
import android.graphics.text.LineBreaker
import android.icu.lang.UCharacter
import android.text.Html
import androidx.lifecycle.AndroidViewModel
import br.com.victoriasantos.womensafe.interactor.DialogFlowInteractor

class DialogFlowViewModel(val app: Application) : AndroidViewModel(app) {
    val interactor = DialogFlowInteractor(app.applicationContext)
    fun sendTextMessage(text: String, sessionId: String, callback: (response: String?) -> Unit) {
        interactor.sendTextMessage(text, sessionId, callback)
    }
}