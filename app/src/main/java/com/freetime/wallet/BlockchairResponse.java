package com.freetime.wallet;

import android.os.Bundle;

public class BlockchairResponse {
    public Map<String, AddressData> data;

    public static class AddressData {
        public Address address;
    }

    public static class Address {
        public long balance;
    }
}