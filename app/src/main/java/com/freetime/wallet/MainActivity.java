package com.freetime.wallet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnGen = findViewById(R.id.btnGen);
        btnGen.setOnClickListener(v -> {
            HDWallet wallet = new HDWallet(128, ""); // strength = 128 bits, empty passphrase
            String mnemonic = wallet.mnemonic();
            String address = wallet.getAddressForCoin(CoinType.ETHEREUM);

            Log.d("Wallet", "Mnemonic: " + mnemonic);
            Log.d("Wallet", "ETH Address: " + address);
        });
    }
}