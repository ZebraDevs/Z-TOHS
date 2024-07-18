package com.zebra.ztohs

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.symbol.emdk.EMDKBase
import com.symbol.emdk.EMDKException
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.ProfileManager


class MainActivity : AppCompatActivity(), EMDKManager.EMDKListener, EMDKManager.StatusListener, ProfileManager.DataListener  {

    private var emdkManager: EMDKManager? = null
    private var profileManager: ProfileManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val results = EMDKManager.getEMDKManager(applicationContext, this)


    }

    override fun onOpened(emdkManager: EMDKManager) {
        this.emdkManager = emdkManager

        try {
             emdkManager.getInstanceAsync(EMDKManager.FEATURE_TYPE.PROFILE, this)
        } catch (e: EMDKException) {

        }

    }

    override fun onClosed() {
        if (emdkManager != null) {
            emdkManager!!.release()
            emdkManager = null
        }
    }

    override fun onStatus(statusData: EMDKManager.StatusData, emdkBase: EMDKBase) {
        if (statusData.result == EMDKResults.STATUS_CODE.SUCCESS) {
            if (statusData.featureType == EMDKManager.FEATURE_TYPE.PROFILE) {
                profileManager = emdkBase as ProfileManager

                if (profileManager != null) {
                    profileManager!!.addDataListener(this)
                    val results = profileManager!!.processProfileAsync(
                        "TOHS",
                        ProfileManager.PROFILE_FLAG.SET,
                        null as Array<String?>?
                    )
                    if (results.statusCode == EMDKResults.STATUS_CODE.PROCESSING) {
                        //Applying the profile, status will be returned through
                        //the registered callback
                    } else {
                        //Failed to initiate request to apply the profiles.
                    }
                } else {
                    //profileManager is null
                }
            }
        } else {
            //Error occurred
        }
    }

    override fun onData(resultData: ProfileManager.ResultData) {
        val result = resultData.result
        if (result.statusCode == EMDKResults.STATUS_CODE.CHECK_XML) {
            val responseXML = result.statusString
            Log.d("EMDK", responseXML)
        } else if (result.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            Log.d("EMDK", "Failed to apply profile")
        }
        Log.d("EMDK", "onData end. Exiting app.")
        Toast.makeText(this, "Hotspot Active", Toast.LENGTH_SHORT).show()
        finishAffinity()
    }


}