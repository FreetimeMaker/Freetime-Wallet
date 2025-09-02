package com.freetime.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import wallet.core.jni.CoinType;
import wallet.core.jni.HDWallet;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("TrustWalletCore");
    }

    private EditText inputPassphrase;
    private Spinner coinSelector;
    private TextView addressDisplay;
    private HDWallet wallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputPassphrase = findViewById(R.id.inputPassphrase);
        coinSelector = findViewById(R.id.coinSelector);
        addressDisplay = findViewById(R.id.addressDisplay);

        MaterialButton btnGen = findViewById(R.id.btnGen);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        coinSelector.setEnabled(false); // Disabled until wallet is ready

        btnGen.setOnClickListener(v -> generateWallet());
        btnLogin.setOnClickListener(v -> loginWithPassphrase());

        coinSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (wallet == null) {
                    addressDisplay.setText("Please generate or login first.");
                    return;
                }

                CoinType selectedCoin = getCoinTypeFromPosition(position);
                String address = wallet.getAddressForCoin(selectedCoin);
                addressDisplay.setText(selectedCoin.name() + " Address:\n" + address);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void generateWallet() {
        wallet = new HDWallet(128, "");
        String mnemonic = wallet.mnemonic();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Wallet Mnemonic", mnemonic);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Passphrase copied to clipboard!", Toast.LENGTH_SHORT).show();
        inputPassphrase.setText(mnemonic);

        coinSelector.setEnabled(true);
        coinSelector.setSelection(0); // Default to Bitcoin

        CoinType selectedCoin = getCoinTypeFromPosition(coinSelector.getSelectedItemPosition());
        String address = wallet.getAddressForCoin(selectedCoin);
        addressDisplay.setText(selectedCoin.name() + " Address:\n" + address);

        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.putExtra("mnemonic", mnemonic);
        startActivity(intent);

    }

    private void loginWithPassphrase() {
        String mnemonic = inputPassphrase.getText().toString().trim();

        if (!HDWallet.isValidMnemonic(mnemonic)) {
            Toast.makeText(this, "Invalid passphrase. Please check and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        wallet = new HDWallet(mnemonic, "");

        coinSelector.setEnabled(true);
        coinSelector.setSelection(0); // Default to Bitcoin

        CoinType selectedCoin = getCoinTypeFromPosition(coinSelector.getSelectedItemPosition());
        String address = wallet.getAddressForCoin(selectedCoin);
        addressDisplay.setText(selectedCoin.name() + " Address:\n" + address);

        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
    intent.putExtra("mnemonic", mnemonic);
    startActivity(intent);

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
