package com.example.rsaproj.ui

import android.os.Bundle
import android.security.keystore.KeyProperties
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.rsaproj.databinding.FragmentEncryptDecryptBinding
import com.example.rsaproj.utils.ClipboardHelper
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class EncryptDecryptFragment : Fragment() {

    companion object {
        private const val KEY_SIZE = 1024
        private const val MESSAGE_ERROR_KEYGEN = "Failed to generate keys"
        private const val MESSAGE_ERROR_TEXT_TOO_LARGE = "Text too large"
        private const val MESSAGE_COPIED = "Copied to clipboard!"
        private const val LABEL_PUBLIC_KEY = "Public key"
        private const val LABEL_ENCRYPTED_TEXT = "Encrypted text"
        private const val LABEL_DECRYPTED_TEXT = "Decrypted text"
    }

    private val clipboard: ClipboardHelper by lazy { ClipboardHelper(requireContext()) }

    // UI Bindings
    private var _binding: FragmentEncryptDecryptBinding? = null
    private val binding get() = _binding!!

    // UI State
    private var isSending = true
    private var textToDecrypt = ""
    private var textToEncrypt = ""
    private var publicKeyOfRecipient = ""
    private var publicKeyEncoded = ""
    private var encryptedText = ""
    private var decryptedText = ""

    // crypto
    private var cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEncryptDecryptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generateKeys()
        setupUI()
        refreshUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Initiates all views */
    private fun setupUI() {
        setupSelector()
        setupSendBlock()
        setupReceiveBlock()
    }

    /** Simply renders UI
     *
     * Called after every action that possibly may have changed UI state so that changes are properly displayed */
    private fun refreshUI() {
        with(binding) {
            sendBlock.isVisible = isSending
            receiveBlock.isVisible = !isSending
            inputTextToEncrypt.editText.setText(textToEncrypt)
            inputTextToDecrypt.editText.setText(textToDecrypt)
            inputTextPublicKey.editText.setText(publicKeyOfRecipient)
            resultEncrypted.isVisible = isSending && encryptedText.isNotBlank()
            resultDecrypted.isVisible = !isSending && decryptedText.isNotBlank()
            textResultEncrypted.text = encryptedText
            textResultDecrypted.text = decryptedText
        }
    }

    private fun setupSelector() {
        binding.selector.setOnSelectedOptionChangeCallback { index ->
            when (index) {
                0 -> isSending = true
                1 -> isSending = false
            }
            refreshUI()
        }
    }

    private fun setupSendBlock() {
        setupTextToEncryptInput()
        setupPublicKeyInput()
        setupEncryptButton()
        setupCopyEncryptedButton()
    }

    private fun setupReceiveBlock() {
        setupCopyPublicButton()
        setupTextToDecryptInput()
        setupDecryptButton()
        setupCopyDecryptedButton()
    }

    private fun setupTextToEncryptInput() {
        with(binding.inputTextToEncrypt) {
            editText.addTextChangedListener { text ->
                textToEncrypt = text.toString()
            }
            setOnButtonClickListener {
                val text = clipboard.getClip()
                textToEncrypt = text ?: ""

                refreshUI()
            }
        }
    }

    private fun setupTextToDecryptInput() {
        with(binding.inputTextToDecrypt) {
            editText.addTextChangedListener { text ->
                textToDecrypt = text.toString()
            }
            setOnButtonClickListener {
                val text = clipboard.getClip()
                textToDecrypt = text ?: ""

                refreshUI()
            }
        }
    }

    private fun setupPublicKeyInput() {
        with(binding.inputTextPublicKey) {
            editText.addTextChangedListener { text ->
                publicKeyOfRecipient = text.toString()
            }
            setOnButtonClickListener {
                val text = clipboard.getClip()
                publicKeyOfRecipient = text ?: ""

                refreshUI()
            }
        }
    }

    private fun setupEncryptButton() {
        binding.buttonEncrypt.setOnClickListener {
            encryptedText = encrypt(publicKeyOfRecipient, textToEncrypt)
            refreshUI()
        }
    }

    private fun setupDecryptButton() {
        binding.buttonDecrypt.setOnClickListener {
            decryptedText = decrypt(textToDecrypt)
            refreshUI()
        }
    }

    private fun setupCopyPublicButton() {
        binding.buttonCopyPublic.setOnClickListener {
            clipboard.copy(publicKeyEncoded, LABEL_PUBLIC_KEY)
            showToast(MESSAGE_COPIED)
        }
    }

    private fun setupCopyEncryptedButton() {
        binding.buttonCopyEncrypted.setOnClickListener {
            clipboard.copy(encryptedText, LABEL_ENCRYPTED_TEXT)
            showToast(MESSAGE_COPIED)
        }
    }

    private fun setupCopyDecryptedButton() {
        binding.buttonCopyDecrypted.setOnClickListener {
            clipboard.copy(decryptedText, LABEL_DECRYPTED_TEXT)
            showToast(MESSAGE_COPIED)
        }
    }

    private fun generateKeys() {
        try {
            val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
            generator.initialize(KEY_SIZE)
            val pair = generator.generateKeyPair()

            privateKey = pair.private
            publicKey = pair.public
            publicKeyEncoded = Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
        } catch (e: Exception) {
            showToast(MESSAGE_ERROR_KEYGEN)
        }
    }

    private fun encrypt(pubKeyEncoded: String, text: String): String {
        val keyBytes = Base64.decode(pubKeyEncoded, Base64.NO_WRAP)
        val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(keyBytes))

        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypted = try {
            cipher.doFinal(text.toByteArray())
        } catch (e: Exception) {
            showToast(MESSAGE_ERROR_TEXT_TOO_LARGE)
            byteArrayOf()
        }
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(text: String): String {
        val bytes = Base64.decode(text, Base64.NO_WRAP)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decrypted = cipher.doFinal(bytes)
        return decrypted.toString(Charset.forName("UTF-8"))
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

}