/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.identity.kotlindemo

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.common.ApiException
import com.huawei.hms.identity.Address
import com.huawei.hms.identity.entity.GetUserAddressResult
import com.huawei.hms.identity.entity.UserAddress
import com.huawei.hms.identity.entity.UserAddressRequest
import com.huawei.identity.kotlindemo.util.LogUtil
import com.huawei.identity.kotlindemo.util.Util
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"
private const val GET_ADDRESS_REQUESTCODE = 1000
private const val SUCCESS = "0"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        query_user_address.apply {
            setOnClickListener {
                if (!Util.isNetworkAvailable(this@MainActivity)) {
                    Toast.makeText(this@MainActivity,
                        R.string.network_not_available,
                        Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                getUserAddress()
            }
        }
    }

    private fun getUserAddress() {
        val task = Address.getAddressClient(this@MainActivity).getUserAddress(UserAddressRequest())
        task.addOnSuccessListener {
            LogUtil.i(TAG, "onSuccess result code: ${it.returnCode}")
            try {
                startActivityForResult(it)
            } catch (ex: IntentSender.SendIntentException) {
                ex.printStackTrace()
            }
        }.addOnFailureListener {
            LogUtil.i(TAG, "on Failed result code: ${it.message}")
            if (it is ApiException) {
                when (it.statusCode) {
                    60054 -> {
                        Toast.makeText(this@MainActivity,
                            R.string.country_not_supported_identity,
                            Toast.LENGTH_SHORT).show()
                    }
                    60055 -> {
                        Toast.makeText(this@MainActivity,
                            R.string.child_account_not_supported_identity,
                            Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this@MainActivity,
                            "errorCode: ${it.statusCode}, errMsg: ${it.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@MainActivity, "unknown exception", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startActivityForResult(result: GetUserAddressResult) {
        val status = result.status
        if (result.returnCode == 0 && status.hasResolution()) {
            LogUtil.i(TAG, "the result had resolution.")
            status.startResolutionForResult(this@MainActivity, GET_ADDRESS_REQUESTCODE)
        } else {
            LogUtil.i(TAG, "the response is wrong, the returnCode is: ${result.returnCode}")
            Toast.makeText(this@MainActivity, "errorCode: ${result.returnCode}, " +
                    "errMsg: ${result.returnDesc}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogUtil.i(TAG, "onActivityResult, requestCode: ${requestCode}, resultCode: ${resultCode}")
        when (requestCode) {
            GET_ADDRESS_REQUESTCODE -> {
                onGetAddressResult(resultCode, data)
            }
        }
    }

    private fun onGetAddressResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val userAddress: UserAddress? = UserAddress.parseIntent(data)
                if (userAddress != null) {
                    val sb = StringBuilder()
                    sb.apply {
                        append("" + userAddress.name)
                        append(" " + userAddress.phoneNumber)
                        append(" " + userAddress.emailAddress)
                        append(System.lineSeparator() + userAddress.countryCode + " ")
                        append(userAddress.administrativeArea)
                        if (!TextUtils.isEmpty(userAddress.locality)) {
                            append(userAddress.locality)
                        }
                        if (!TextUtils.isEmpty(userAddress.addressLine1)) {
                            append(userAddress.addressLine1)
                        }
                        append(userAddress.addressLine2)
                        if (!TextUtils.isEmpty(userAddress.postalNumber)) {
                            append(System.lineSeparator() + userAddress.postalNumber)
                        }
                    }
                    user_address.setText(sb.toString());
                } else {
                    user_address.setText("Failed to get user address.")
                    Toast.makeText(this@MainActivity,
                        "the user address is null.",
                        Toast.LENGTH_SHORT).show()
                }
            }
            Activity.RESULT_CANCELED -> {
                Toast.makeText(this@MainActivity,
                    R.string.user_cancel_share_address,
                    Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this@MainActivity,
                    "user address is null, errorCode: $resultCode",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }
}