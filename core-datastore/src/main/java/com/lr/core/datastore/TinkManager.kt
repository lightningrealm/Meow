package com.lr.core.datastore

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

object TinkManager {

    private const val KEYSET_NAME = "cookie_keyset"
    private const val PREF_FILE_NAME = "meow_tink_prefs"
    private const val MASTER_KEY_URI = "android-keystore://meow_master_key"
    
    private var aead: Aead? = null
    
    fun init(context: Context) {
        if (aead != null) return
        
        // Register all AEAD primitive wrappers (e.g. AES-GCM)
        AeadConfig.register()
        
        // Generate or load the keyset stored in SharedPreferences, encrypted by Android Keystore
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context.applicationContext, KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            
        aead = keysetHandle.getPrimitive(Aead::class.java)
    }
    
    fun encrypt(plainText: String): String {
        val aeadInstance = requireNotNull(aead) { "TinkManager must be initialized before use" }
        val cipherText = aeadInstance.encrypt(plainText.toByteArray(Charsets.UTF_8), null)
        return Base64.encodeToString(cipherText, Base64.NO_WRAP)
    }
    
    fun decrypt(cipherTextBase64: String): String? {
        val aeadInstance = requireNotNull(aead) { "TinkManager must be initialized before use" }
        return try {
            val cipherText = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
            val plainText = aeadInstance.decrypt(cipherText, null)
            String(plainText, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
