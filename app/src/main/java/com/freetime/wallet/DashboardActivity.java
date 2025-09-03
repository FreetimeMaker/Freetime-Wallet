package com.freetime.wallet;

import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import wallet.core.jni.CoinType;
import wallet.core.jni.HDWallet;

public class DashboardActivity extends AppCompatActivity {

    ImageView qrCodeImage;
    private Spinner coinSelector;
    private TextView addressDisplay;
    private HDWallet wallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        coinSelector = findViewById(R.id.coinSelector);
        addressDisplay = findViewById(R.id.addressDisplay);
        qrCodeImage = findViewById(R.id.qrCodeImage);

        String mnemonic = getIntent().getStringExtra("mnemonic");
        wallet = new HDWallet(mnemonic, "");

        coinSelector.setOnItemSelectedListener((parent, view, position, id) -> {
            CoinType selectedCoin = getCoinTypeFromPosition(position);
            String address = wallet.getAddressForCoin(selectedCoin);
            addressDisplay.setText(selectedCoin.name() + " Address:\n" + address);

            // Generate QR code
            Bitmap qrBitmap = new BarcodeEncoder().encodeBitmap(address, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImage.setImageBitmap(qrBitmap);
        });

        coinSelector.setSelection(0); // Default to Bitcoin

        public interface BlockchairApi {
            @GET("bitcoin/dashboards/address/{address}")
            Call<BlockchairResponse> getBitcoinBalance(@Path("address") String address);
        }

        Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://api.blockchair.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build();

        BlockchairApi api = retrofit.create(BlockchairApi.class);

        api.getBitcoinBalance(address).enqueue(new Callback<BlockchairResponse>() {
        @Override
        public void onResponse(Call<BlockchairResponse> call, Response<BlockchairResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                long satoshis = response.body().data.get(address).address.balance;
                double btc = satoshis / 100_000_000.0;

                runOnUiThread(() -> {
                    addressDisplay.append("\nBalance: " + btc + " BTC");
                });
            }
        }

        @Override
        public void onFailure(Call<BlockchairResponse> call, Throwable t) {
            runOnUiThread(() -> {
                Toast.makeText(DashboardActivity.this, "Failed to fetch balance", Toast.LENGTH_SHORT).show();
            });
        }
        });

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Clear sensitive data if stored
            wallet = null;

            // Optionally clear shared preferences or cache
            // getSharedPreferences("wallet", MODE_PRIVATE).edit().clear().apply();

            // Navigate back to MainActivity
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Prevent back navigation
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
