package com.sample.katsupay.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sample.katsupay.*
import com.sample.katsupay.communication.CommServer
import com.sample.katsupay.data.DataChecker
import com.sample.katsupay.data.JsonParser
import com.sample.katsupay.data.UserInfo
import kotlinx.android.synthetic.main.sign_in.*
import java.net.HttpURLConnection


class SignIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in)
    }

    override fun onResume(){
        super.onResume()
        signIn.setOnClickListener {
            val password = passwordEdit.text.toString()
            val userName = userNameEdit.text.toString()
            if(checkCorrectEntered(userName, password)) {
                UserInfo.customer_id = userName
                UserInfo.initPassword(password)
                if(!signIn()) {
                    passwordEdit.text.clear()
                }
            }
        }

        SignUpButton.setOnClickListener {
            val intent = Intent(this, ScanQRCode::class.java)
            startActivity(intent)
        }

        contact.setOnClickListener {
            val cu = Intent(this, ContactUs::class.java)
            startActivity(cu)
        }

        store_stock.setOnClickListener {
            val allstores =Intent(this, AllStores::class.java)
            startActivity(allstores)
        }

        if(!CommServer.isConnected(this)) {
            val intent = Intent(this, Reconnect::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        /* do nothing */
    }

    private fun signIn() : Boolean {
        val commServer = CommServer()
        commServer.setUrl(CommServer.LOGIN)
        commServer.execute(CommServer.UB)

        while(commServer.responseCode == -1) { /* wait for response */ }

        if(commServer.responseCode == HttpURLConnection.HTTP_OK) {
            val response = commServer.get() ?: return false
            Log.i("RETURN VALUE FROM SERVER", " VALUE: $response")
            if(response != "null") {
                val customer = JsonParser.customerParse(response)
                return if(customer == null) {
                    Toast.makeText(this, "ログインの際にサーバから予期せぬメッセージを受信しました", Toast.LENGTH_LONG).show()
                    false
                } else {
                    UserInfo.initialize(customer)
                    Toast.makeText(this, "${UserInfo.customer_id}さん ようこそ！", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
            } else {
                incorrectSignIn(this)
                return false
            }
        } else {
            incorrectSignIn(this)
            return false
        }
    }

    private fun incorrectSignIn(context:Context) {
        AlertDialog.Builder(context)
            .setTitle("●サインイン失敗")
            .setMessage("ユーザ名もしくはパスワードが間違っています")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun checkCorrectEntered(userName:String, password:String) : Boolean {
        Log.e(">>>","USERNAME:$userName, PASSWORD:$password")

        /* 文字列が入力されていない */
        if(userName.isEmpty() || password.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("●サインイン失敗")
                .setMessage("ユーザ名もしくはパスワードが入力されていません")
                .setPositiveButton("OK") { _, _ -> }
                .show()
            return false
        }

        /* ユーザ名の確認 */
        if(!DataChecker.isUserId(userName, this)) return false

        /* パスワードの確認 */
        if(!DataChecker.isPassword(password, this)) return false
        return true
    }
}