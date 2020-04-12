package br.com.victoriasantos.womensafe.repository

import android.content.ComponentCallbacks
import android.content.Context
import android.content.Intent
import android.widget.Toast
import br.com.victoriasantos.womensafe.domain.Profile
import br.com.victoriasantos.womensafe.view.activity.MainActivity
import br.com.victoriasantos.womensafe.view.activity.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_reset_password.*


class FirebaseRepository (context: Context) {
    private val mAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private var profile: Profile? = null


    fun cadastro(email: String, senha: String, callback: (result: String) -> Unit){
        val operation = mAuth.createUserWithEmailAndPassword(email, senha)
        operation.addOnCompleteListener { task ->
            if(task.isSuccessful){
                callback("S")
            }
            else{
                val error = task.exception?.localizedMessage
                    ?: "Não foi possível entrar no aplicativo no momento"
                    callback(error)

            }
        }
    }



    fun consulta(callback: (snapshot: DataSnapshot?) -> Unit){
        val email = mAuth.currentUser?.email
        val profiles = database.getReference("Usuários")
        val query = profiles.orderByChild("email").equalTo(email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                callback(null)
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot)
            }
        })

    }

    fun login(email: String, senha: String, callback: (result: String) -> Unit) {
        val operation = mAuth.signInWithEmailAndPassword(email, senha)
        operation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
               callback("S")
            } else {
                val error = task.exception?.localizedMessage
                    ?: "Não foi possivel entrar o aplicativo"
                callback(error)
            }
        }
    }

    fun getEmail(callback: (email: String?) -> Unit){
        var emailFinal = mAuth.currentUser?.email
        callback(emailFinal)
    }

    fun UpdateEmail(email: String, callback: (result: String) -> Unit ){

        val usuario = mAuth.currentUser

        usuario?.updateEmail(email)?.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                val error = task.exception?.localizedMessage
                    ?: "Houve um erro ao atualizar o e-mail!"
                callback(error)
            } else {
                callback("S")
            }
        }
    }

    fun saveData(email: String, nomecompleto: String, telefone: String, username: String, callback: (result: String) -> Unit){
            profile = Profile(
                email = email,
                nomecompleto = nomecompleto,
                telefone = telefone,
                username = username
            )

            val uid = mAuth.currentUser?.uid

            if(uid != null){
                // Variável que define qual nó será atualizado, nesse caso será o nó "Usuários"
                val userprofile = database.getReference("Usuários/$uid") // $uid é o onde será substituido pelo id do usuário logado
                    userprofile.setValue(profile) //Atualiza/cria os dados
                    callback("SUCCESS")

            }
            else
            {
                callback("UID RECOVER FAIL")
            }

        }

        fun deleteUser(callback: (result: String) -> Unit){
            //exclui a conta na autenticação
            val usuario = mAuth.currentUser
            var erro: Int = 0;
            var message: String = ""

            usuario?.delete()?.addOnCompleteListener { task ->
                if(!task.isSuccessful){
                    val error = task.exception?.localizedMessage
                        ?:"Não foi possível excluir este usuário!"
                    erro = 1
                    message = error
                }

            }
            // exclui no banco de dados
            val uid = usuario?.uid
            val dados = database.getReference("Usuários/$uid")

            dados.removeValue().addOnCompleteListener { task ->
                if(!task.isSuccessful) {
                    val error = task.exception?.localizedMessage
                        ?:"Não foi possível excluir os dados!"
                    erro = 1
                    message = error
                }
            }

            if(erro == 0){
            callback("SUCCESS")
            }
            else{
                callback(message)
            }
        }


        fun changePassword(email: String){
            mAuth.sendPasswordResetEmail(email)
        }

    }
