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

package com.huawei.identity.demo.util

import android.content.Context
import android.net.ConnectivityManager

object Util {
    fun isNetworkAvailable(context: Context?): Boolean {
        val connectivityManager =
            (context?.getSystemService(Context.CONNECTIVITY_SERVICE)) as ConnectivityManager?
        val networkInfo = connectivityManager?.activeNetworkInfo
        return (networkInfo?.isAvailable) ?: false
    }
}