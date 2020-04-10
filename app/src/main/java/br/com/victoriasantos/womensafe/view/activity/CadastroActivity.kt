package br.com.victoriasantos.womensafe.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import br.com.victoriasantos.womensafe.R
import br.com.victoriasantos.womensafe.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_cadastro.*

class CadastroActivity : AppCompatActivity() {
    private val viewModel: FirebaseViewModel by lazy {
        ViewModelProvider(this). get(FirebaseViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        cadastrobt.setOnClickListener { cadastrar() }

    }

    private fun cadastrar(){
        val email = emailet.text.toString()
        val senha = senhaet.text.toString()

        viewModel.cadastro(email, senha) { result ->

            if(result == "S") {
                val intentToProfile = Intent(this, ProfileActivity::class.java)
                startActivity(intentToProfile)
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
            }
        }

    }
}