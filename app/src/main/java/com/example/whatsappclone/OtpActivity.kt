package com.example.whatsappclone

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.example.whatsappclone.auth.LoginActivity
import com.example.whatsappclone.auth.SignUpActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
//import com.google.firebase.auth
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "phoneNumber"

class OtpActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth

    val TAG = "OtpActivity"
    var phoneNumber: String? = null
    private var mVerificationId: String? = null
    private lateinit var mResendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var progressDialog: ProgressDialog
    private var mCounterDown: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        // Initialize Firebase Auth
//        auth = Firebase.auth
        // [END initialize_auth]

        initViews()

        startVerify()

    }

    private fun startVerify() {
        Log.d(TAG, "Start of startVerify")
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber!!,          //Phone number to verify
            60,             //Timeout duration
            TimeUnit.SECONDS,       //Unit of timeout
            this,           //Activity for callback binding
            callbacks               //OnVerificationStateChangedCallbacks
        )

        Log.d(TAG, "middle of startVerify")
        //show timer while recieving OTP
        showTimer(60000)

        //Initializing progress Dialogue
        progressDialog = createProgressDialog("Sending  a verification code", false)
        progressDialog.show()

        Log.d(TAG, "end of startVerify")
    }

    private fun initViews() {
        //Getting the data from the LoginActivity
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        verifyTv.text = getString(R.string.verify_number, phoneNumber)
        setSpannableString()

        verificationBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(
                    TAG,
                    "onVerificationCompleted:$credential \n credentialCode : ${credential.smsCode}"
                )

                //Dismissing Progress Dialogue
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }

                val smsCode = credential.smsCode
                if (!smsCode.isNullOrBlank()) {
                    sentcodeEt.setText(smsCode)
                }
                Log.d(TAG, "smsCode : $smsCode")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }
                Log.d(TAG, e.localizedMessage)
                // Show a message and update the UI
                notifyUserAndRetry("Your Phone Number might be wrong or connection error.Retry Again")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                //Dismissing Progress Dialogue
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
                counterTv.isVisible = false

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token
            }
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    startActivity(Intent(this, SignUpActivity::class.java))

                } else {
                    // Sign in failed, display a message and update the UI
                    notifyUserAndRetry("Your Phone number verification failed.Try Again !!")
                    Log.w(TAG, "signInWithCredential:failure", it.exception)
                }
            }
    }

    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(
                "We will be Verifying the $phoneNumber\n" +
                        "Is this OK, or Would you like to edit the number?"
            )
            setPositiveButton("Ok") { _, _ ->
                showLoginActivity()
            }
            setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }

    }

    //For timer of 60 Seconds until OTP code recieved
    private fun showTimer(milliSecInFuture: Long) {
        resendBtn.isEnabled = false
        mCounterDown = object : CountDownTimer(milliSecInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counterTv.isVisible = true
                counterTv.text = getString(R.string.second_remaining, millisUntilFinished / 1000)
            }

            override fun onFinish() {
                counterTv.isVisible = false
                resendBtn.isEnabled = true
            }

        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCounterDown != null) {
            mCounterDown!!.cancel()
        }
    }


    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_text, phoneNumber))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                //send back
                showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = ds.linkColor
            }
        }

        span.setSpan(clickableSpan, span.length - 13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod = LinkMovementMethod.getInstance()
        waitingTv.text = span
    }

    private fun showLoginActivity() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        //these flags are to ensure that user can not go back to the previous Activity
    }

    //this will stop the user from going to the back activity that is LoginActivity
    override fun onBackPressed() {

    }

    override fun onClick(v: View?) {
        when (v) {
            verificationBtn -> {
                val code = sentcodeEt.text.toString()
                if (code.isNotEmpty() && !mVerificationId.isNullOrBlank()) {
                    progressDialog = createProgressDialog("Please wait...", false)
                    progressDialog.show()

                    val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
                    signInWithPhoneAuthCredential(credential)
                }
            }
            resendBtn -> {
                val code = sentcodeEt.text.toString()
                if (mResendToken != null) {
                    showTimer(600000)
                    progressDialog = createProgressDialog("Sending a verification code", false)
                    progressDialog.show()

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber!!,          //Phone number to verify
                        60,             //Timeout duration
                        TimeUnit.SECONDS,       //Unit of timeout
                        this,           //Activity for callback binding
                        callbacks,               //OnVerificationStateChangedCallbacks
                        mResendToken
                    )

                }
            }
        }
    }
}

fun Context.createProgressDialog(message: String,isCancelable:Boolean):ProgressDialog{
    return ProgressDialog(this).apply {
        setCancelable(false)
        setMessage(message)
        setCanceledOnTouchOutside(false)
    }
}