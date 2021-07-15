package com.huawei.demo.identitydemo;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.demo.log.LogUtil;
import com.huawei.demo.util.Util;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.identity.Address;
import com.huawei.hms.identity.entity.GetUserAddressResult;
import com.huawei.hms.identity.entity.UserAddress;
import com.huawei.hms.identity.entity.UserAddressRequest;
import com.huawei.hms.support.api.client.Status;
import java.util.ArrayList;
import java.util.HashMap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
	   private static final String TAG = "identitycorelab";

	    private static final int GET_ADDRESS = 1000;

	    private TextView textView;

	    private ListView listView;

	    private Button queryAddrButton;

	    private int[] ids = {R.id.user_address_nickname,R.id.user_address_phone,R.id.user_address_email, R.id.user_address_detailaddress};
	    private String[] strings = {"nickname","phone","email","detailaddress"};

	    private ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        textView = findViewById(R.id.demo_introduce);
	        queryAddrButton = findViewById(R.id.query_user_address);
	        queryAddrButton.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
//	                if (!Util.isNetworkAvailable(MainActivity.this)) {
//	                    Toast.makeText(getApplicationContext(), R.string.network_not_available, Toast.LENGTH_SHORT).show();
//	                    return;
//	                }
	                getUserAddress();
	            }
	        });

	        listView = findViewById(R.id.address_listview);
	        // HashMap<String, Object> map = new HashMap<String, Object>();
	        // map.put("nickname", "Ben");
	        // map.put("phone", "44 15687654321");
	        // map.put("email", "Ben@abc.com");
	        // map.put("detailaddress","United Kingdom London Street 1" );
	        // list.add(map);

	        SimpleAdapter simpleAdapter = new SimpleAdapter(this,list,R.layout.address_item, strings,ids);
	        listView.setAdapter(simpleAdapter);
	    }

	    private void getUserAddress() {
	        UserAddressRequest req = new UserAddressRequest();
	        Task<GetUserAddressResult> task = Address.getAddressClient(this).getUserAddress(req);
	        task.addOnSuccessListener(new OnSuccessListener<GetUserAddressResult>() {
	            @Override
	            public void onSuccess(GetUserAddressResult result) {
	                LogUtil.i(TAG, "onSuccess result code:" + result.getReturnCode());
	                try {
	                    startActivityForResult(result);
	                } catch (IntentSender.SendIntentException e) {
	                    e.printStackTrace();
	                }
	            }
	        }).addOnFailureListener(new OnFailureListener() {
	            @Override
	            public void onFailure(Exception e) {
	                LogUtil.i(TAG, "on Failed result code:" + e.getMessage());
	                if (e instanceof ApiException) {
	                    ApiException apiException = (ApiException) e;
	                    switch (apiException.getStatusCode()) {
	                        case 60054:
	                            Toast.makeText(getApplicationContext(), R.string.country_not_supported_identity, Toast.LENGTH_SHORT).show();
	                            break;
	                        case 60055:
	                            Toast.makeText(getApplicationContext(), R.string.child_account_not_supported_identity, Toast.LENGTH_SHORT).show();
	                            break;
	                        default: {
	                            Toast.makeText(getApplicationContext(), "errorCode:" + apiException.getStatusCode() + ", errMsg:" + apiException.getMessage(), Toast.LENGTH_SHORT).show();
	                        }
	                    }
	                } else {
	                    Toast.makeText(getApplicationContext(), "unknown exception", Toast.LENGTH_SHORT).show();
	                }
	            }
	        });

	    }

	    private void startActivityForResult(GetUserAddressResult result) throws IntentSender.SendIntentException {
	        Status status = result.getStatus();
	        if (result.getReturnCode() == 0 && status.hasResolution()) {
	            LogUtil.i(TAG, "the result had resolution.");
	            status.startResolutionForResult(this, GET_ADDRESS);
	        } else {
	            LogUtil.i(TAG, "the response is wrong, the return code is " + result.getReturnCode());
	            Toast.makeText(getApplicationContext(), "errorCode:" + result.getReturnCode() + ", errMsg:" + result.getReturnDesc(), Toast.LENGTH_SHORT).show();
	        }
	    }

	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	        LogUtil.i(TAG, "onActivityResult requestCode " + requestCode + " resultCode " + resultCode);
	        switch (requestCode) {
	            case GET_ADDRESS:
	                switch (resultCode) {
	                    case Activity.RESULT_OK:
	                        UserAddress userAddress = UserAddress.parseIntent(data);
	                        if (userAddress != null) {
	                            textView.setVisibility(View.GONE);
	                            StringBuilder sb = new StringBuilder();
	                            sb.append(userAddress.getCountryCode() + " ");
	                            sb.append(userAddress.getAdministrativeArea());
	                            if (userAddress.getLocality() != null) {
	                                sb.append(userAddress.getLocality());
	                            }
	                            if (userAddress.getAddressLine1() != null) {
	                                sb.append(userAddress.getAddressLine1());
	                            }
	                            sb.append(userAddress.getAddressLine2());
	                            HashMap<String, Object> map = new HashMap<String, Object>();
	                            map.put("nickname", userAddress.getName());
	                            map.put("phone", userAddress.getPhoneNumber());
	                            map.put("email", userAddress.getEmailAddress());
	                            map.put("detailaddress",sb );
	                            list.add(map);
	                            listView.invalidateViews();
	                        } else {
	                            textView.setText("Failed to get user address.");
	                            Toast.makeText(getApplicationContext(), "the user address is null.", Toast.LENGTH_SHORT).show();
	                        }
	                        break;
	                    case Activity.RESULT_CANCELED:
	                        Toast.makeText(getApplicationContext(), R.string.user_cancel_share_address, Toast.LENGTH_SHORT).show();
	                        break;
	                    default:
	                        LogUtil.i(TAG, "result is wrong, result code is " + resultCode);
	                        Toast.makeText(getApplicationContext(), "the user address is null.", Toast.LENGTH_SHORT).show();
	                        break;
	                }
	            default:
	                break;
	        }
	    }


}
