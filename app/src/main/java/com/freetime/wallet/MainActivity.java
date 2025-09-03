package com.freetime.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Currency;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

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
    private TextView priceDisplay;
    private HDWallet wallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputPassphrase = findViewById(R.id.inputPassphrase);
        coinSelector = findViewById(R.id.coinSelector);
        addressDisplay = findViewById(R.id.addressDisplay);
        priceDisplay = findViewById(R.id.priceDisplay); // Add this TextView in XML

        MaterialButton btnGen = findViewById(R.id.btnGen);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        coinSelector.setEnabled(false);

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

                fetchCryptoPrice(selectedCoin);
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
        coinSelector.setSelection(0);

        CoinType selectedCoin = getCoinTypeFromPosition(coinSelector.getSelectedItemPosition());
        String address = wallet.getAddressForCoin(selectedCoin);
        addressDisplay.setText(selectedCoin.name() + " Address:\n" + address);

        fetchCryptoPrice(selectedCoin);

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
        coinSelector.setSelection(0);

        CoinType selectedCoin = getCoinTypeFromPosition(coinSelector.getSelectedItemPosition());
        String address = wallet.getAddressForCoin(selectedCoin);
        addressDisplay.setText(selectedCoin.name() + " Address:\n" + address);

        fetchCryptoPrice(selectedCoin);

        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.putExtra("mnemonic", mnemonic);
        startActivity(intent);
    }

    private void fetchCryptoPrice(CoinType coinType) {
        String currencyCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode(); // e.g. "CHF"
        String coinId = getCoinGeckoId(coinType); // e.g. "bitcoin"

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + coinId + "&vs_currencies=" + currencyCode.toLowerCase();

        Request request = new Request.Builder().url(url).build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String json = response.body().string();

                JSONObject jsonObject = new JSONObject(json);
                double price = jsonObject.getJSONObject(coinId).getDouble(currencyCode.toLowerCase());

                runOnUiThread(() -> {
                    priceDisplay.setText(coinId.toUpperCase() + " Price: " + price + " " + currencyCode);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> priceDisplay.setText("Price unavailable"));
            }
        }).start();
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

    private String getCoinGeckoId(CoinType coinType) {
        switch (coinType) {
            case BITCOIN: return "bitcoin";
            case BITCOINCASH: return "bitcoin-cash";
            case LITECOIN: return "litecoin";
            case ETHEREUM: return "ethereum";
            default: return "ethereum";
        }
    }
}
