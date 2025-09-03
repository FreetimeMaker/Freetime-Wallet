package com.freetime.wallet;

import android.os.Bundle;

import java.util.Map;

public class BlockchairResponse {
    public Map<String, AddressData> data;

    public static class AddressData {
        public Address address;
    }

    public static class Address {
        public long balance;
    }
}