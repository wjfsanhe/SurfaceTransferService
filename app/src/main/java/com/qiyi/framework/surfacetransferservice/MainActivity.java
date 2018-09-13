package com.qiyi.framework.surfacetransferservice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		AIDLSurfaceTransferService.setActivity(this);
		Intent intent = new Intent(this, AIDLSurfaceTransferService.class);
		startService(intent);
	}
}
