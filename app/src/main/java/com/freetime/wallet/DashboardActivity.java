package com.freetime.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import wallet.core.jni.CoinType;
import wallet.core.jni.HDWallet;

public class DashboardActivity extends AppCompatActivity {

    static {
        System.loadLibrary("TrustWalletCore");
    }

    private TextView addressDisplay;
    private HDWallet wallet;

    // Retrofit API interface at class level
    public interface BlockchairApi {
        @GET("bitcoin/dashboards/address/{address}")
        Call<BlockchairResponse> getBitcoinBalance(@Path("address") String address);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        addressDisplay = findViewById(R.id.addressDisplay);

        String mnemonic = getIntent().getStringExtra("mnemonic");
        wallet = new HDWallet(mnemonic, "");

        String btcAddress = wallet.getAddressForCoin(CoinType.BITCOIN);
        String ethAddress = wallet.getAddressForCoin(CoinType.ETHEREUM);
        String ltcAddress = wallet.getAddressForCoin(CoinType.LITECOIN);
        String bchAddress = wallet.getAddressForCoin(CoinType.BITCOINCASH);
        String usdtAddress = ethAddress; // USDT on Ethereum

        // Show BTC address immediately
        addressDisplay.setText("BTC Address:\n" + btcAddress);

        // Retrofit setup
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.blockchair.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BlockchairApi api = retrofit.create(BlockchairApi.class);

        // Fetch BTC balance
        api.getBitcoinBalance(btcAddress).enqueue(new Callback<BlockchairResponse>() {
            @Override
            public void onResponse(Call<BlockchairResponse> call, Response<BlockchairResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long satoshis = response.body().data.get(btcAddress).address.balance;
                    double btc = satoshis / 100_000_000.0;

                    runOnUiThread(() -> {
                        addressDisplay.append("\nBalance: " + btc + " BTC");
                    });
                }
            }

            @Override
            public void onFailure(Call<BlockchairResponse> call, Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(DashboardActivity.this, "Failed to fetch balance", Toast.LENGTH_SHORT).show()
                );
            }
        });
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

