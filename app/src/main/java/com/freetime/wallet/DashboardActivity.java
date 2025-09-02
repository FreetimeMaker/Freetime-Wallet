package com.freetime.wallet;

import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import wallet.core.jni.CoinType;
import wallet.core.jni.HDWallet;

public class DashboardActivity extends AppCompatActivity {

    private Spinner coinSelector;
    private TextView addressDisplay;
    private HDWallet wallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        coinSelector = findViewById(R.id.coinSelector);
        addressDisplay = findViewById(R.id.addressDisplay);

        String mnemonic = getIntent().getStringExtra("mnemonic");
        wallet = new HDWallet(mnemonic, "");

        coinSelector.setOnItemSelectedListener((parent, view, position, id) -> {
            CoinType selectedCoin = getCoinTypeFromPosition(position);
            String address = wallet.getAddressForCoin(selectedCoin);
            addressDisplay.setText(selectedCoin.name() + " Address:\n" + address);
        });

        coinSelector.setSelection(0); // Default to Bitcoin
    }

    private CoinType getCoinTypeFromPosition(int position) {
        switch (position) {
            case 0: return CoinType.BITCOIN;
            case 1: return CoinType.BITCOINCASH;
            case 2: return CoinType.LITECOIN;
            case 3: return CoinType.ETHEREUM;
            default: return CoinType.ETHEREUM;
        }
    }
}
