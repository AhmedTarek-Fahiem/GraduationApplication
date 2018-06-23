package com.example.ahmed_tarek.graduationapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/11/23.
 */

public class CartFragment extends Fragment implements AsyncResponse {

    private DrawerInterface mDrawerInterface;
    private CartMedicineAdapter mCartMedicineAdapter;
    private TextView mTotalPrice;
    private PrescriptionHandler mPrescriptionHandler;
    private static String cartMedicines;
    private static boolean isRegular;

    NetworkChangedReceiver receiver;

    public class NetworkChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                final NetworkInfo ni = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

                TextView mWarning = getView().findViewById(R.id.internet_warning);
                if (ni != null && ni.isConnectedOrConnecting()) {
                    if (MainActivity.notSynced) {
                        try {
                            Linker.getInstance(getActivity(), getView()).syncOperation(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    mWarning.setBackgroundColor(getResources().getColor(R.color.backgroundTintGreen));
                    mWarning.setText(R.string.connected);
                } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                    mWarning.setBackgroundColor(getResources().getColor(R.color.errorMessage));
                    mWarning.setText(R.string.no_internet);
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        receiver = new NetworkChangedReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        getContext().registerReceiver(receiver, intentFilter);

        try {
            mDrawerInterface = (DrawerInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement DrawerInterface");
        }
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String medicinesToString(List<CartMedicine> medicines, Context context) {
        String cartMedicines = "";
        int size = medicines.size();
        for (int i = 0; i < size; i++) {
            cartMedicines = cartMedicines.concat(MedicineLab.get(context).getMedicine(medicines.get(i).getMedicineID()).getName() + '#' + String.valueOf(medicines.get(i).getQuantity()));
            if (medicines.get(i).getRepeatDuration() == 7 || medicines.get(i).getRepeatDuration() == 30)
                isRegular = true;
            if (i != size - 1)
                cartMedicines = cartMedicines.concat("|");
        }
        return cartMedicines;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cart_fragment, container, false);

        TextView mWarning = view.findViewById(R.id.internet_warning);
        if (Linker.getInstance(getActivity(), getView()).checkState(getContext())) {
            mWarning.setBackgroundColor(Color.GREEN);
            mWarning.setText(R.string.connected);
        }
        mDrawerInterface.lockDrawer();
        setHasOptionsMenu(false);

        RecyclerView mCartListRecyclerView = view.findViewById(R.id.cart_list_recycler_view);
        mCartListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mPrescriptionHandler = PrescriptionHandler.get();

        if (mCartMedicineAdapter == null) {
            mCartMedicineAdapter = new CartMedicineAdapter(mPrescriptionHandler.getPrescriptionCartMedicines());
            mCartListRecyclerView.setAdapter(mCartMedicineAdapter);
        } else {
            mCartMedicineAdapter.notifyDataSetChanged();
        }

        mTotalPrice = view.findViewById(R.id.total_payment_number);

        Button mGenerateButton = view.findViewById(R.id.generate_qr);
        mGenerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<CartMedicine> qrMedicines = mPrescriptionHandler.getPrescriptionCartMedicines();

                cartMedicines = "";
                UUID id = mPrescriptionHandler.getPrescription().getID();
                cartMedicines = medicinesToString(qrMedicines, getContext());
                try {
                    List<Prescription> list = new ArrayList<>();
                    list.add(mPrescriptionHandler.getPrescription());
                    mPrescriptionHandler.setPrescriptionPrice(Double.parseDouble(mTotalPrice.getText().toString()));
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putLong(UserLab.get(getContext()).getUsername() + "_recentDate", list.get(0).getDate().getTime()).apply();
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(UserLab.get(getContext()).getUsername() + "_isDoctorPrescription", false).apply();

                    if (isRegular)
                        Linker.getInstance(getActivity(), getView()).schedule(getContext(), id);
                    if (Linker.getInstance(getActivity(), getView()).checkState(getContext()))
                        new MainActivity.DatabaseComm(CartFragment.this, getActivity(), MainActivity.TAG_PRESCRIPTION).execute(MainActivity.toJSON(list, getActivity(), 0, false));
                    else {
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putLong(UserLab.get(getContext()).getUsername() + "_lastPrescription", mPrescriptionHandler.getPrescription().getDate().getTime() + 100).apply();
                        startActivity(QRActivity.newIntent(getContext(), cartMedicines, false));
                    }
                    mPrescriptionHandler.prescriptionCommit(getActivity());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        updatePrice();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void processFinish(JSONObject output, String type) {
        switch (type) {
            case MainActivity.TAG_PRESCRIPTION:
                try {
                    long time = System.currentTimeMillis();
                    String outOfStock = output.getString("outOfStockMedicines");
                    if (output.getInt(MainActivity.TAG_SUCCESS) == 0)
                        MainActivity.showToast(R.string.database_error, getContext());
                    else if (outOfStock != null) {
                        if (outOfStock.length() > 0) {
                            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putLong(UserLab.get(getContext()).getUsername() + "_lastPrescription", time).apply();
                            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putLong(UserLab.get(getContext()).getUsername() + "_lastUpdated", time).apply();
                            QRActivity.MyDialogFragment.newInstance(outOfStock, cartMedicines).show(getFragmentManager().beginTransaction(), "dialog");
                        }
                    } else {
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putLong(UserLab.get(getContext()).getUsername() + "_lastPrescription", time).apply();
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putLong(UserLab.get(getContext()).getUsername() + "_lastUpdated", time).apply();
                        startActivity(QRActivity.newIntent(getContext(), cartMedicines, false));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private class CartMedicineHolder extends RecyclerView.ViewHolder {

        private CartMedicine mCartMedicine;

        private TextView mCartMedicineNameTextView;
        private EditText mCartMedicineQuantity;
        private Spinner mCartMedicineRepeat;
        private ImageButton mRemoveCart;

        public CartMedicineHolder(View itemView) {
            super(itemView);

            mCartMedicineNameTextView = itemView.findViewById(R.id.cart_medicine_name);
            mCartMedicineQuantity = itemView.findViewById(R.id.cart_medicine_quantity);
            mCartMedicineRepeat = itemView.findViewById(R.id.cart_medicine_regular_spinner);
            mRemoveCart = itemView.findViewById(R.id.remove_cart);
        }

        public void bindCartMedicine(CartMedicine cartMedicine) {

            mCartMedicine = cartMedicine;

            mCartMedicineNameTextView.setText(MedicineLab.get(getActivity()).getMedicine(mCartMedicine.getMedicineID()).getName());
            mCartMedicineQuantity.setText(String.valueOf(mCartMedicine.getQuantity()));
            switch (mCartMedicine.getRepeatDuration()) {
                case 0:
                    mCartMedicineRepeat.setSelection(0);
                    break;
                case 7:
                    mCartMedicineRepeat.setSelection(1);
                    break;
                case 30:
                    mCartMedicineRepeat.setSelection(2);
                    break;
                default:
                    mCartMedicineRepeat.setSelection(0);
            }

            mCartMedicineQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                    if (charSequence.length() != 0) {
                        mPrescriptionHandler.setCartMedicineQuantity(mCartMedicine.getMedicineID(), Integer.parseInt(charSequence.toString()));
                    } else {
                        mPrescriptionHandler.setCartMedicineQuantity(mCartMedicine.getMedicineID(), 1);
                    }
                    updatePrice();
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });

            mCartMedicineRepeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    String selectedItem = adapterView.getItemAtPosition(position).toString();
                    if (selectedItem.equals(getResources().getStringArray(R.array.regular_label)[0])) {
                        mPrescriptionHandler.setCartMedicineRepeatDuration(mCartMedicine.getMedicineID(), 0);
                    } else if (selectedItem.equals(getResources().getStringArray(R.array.regular_label)[1])) {
                        mPrescriptionHandler.setCartMedicineRepeatDuration(mCartMedicine.getMedicineID(), 7);
                    } else {
                        mPrescriptionHandler.setCartMedicineRepeatDuration(mCartMedicine.getMedicineID(), 30);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            mRemoveCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPrescriptionHandler.removeCart(mCartMedicine);
                    mCartMedicineAdapter.updateList(mPrescriptionHandler.getPrescriptionCartMedicines());
                    if (mPrescriptionHandler.isEmpty()) {
                        getFragmentManager().popBackStackImmediate();
                    }
                }
            });
        }
    }

    private class CartMedicineAdapter extends RecyclerView.Adapter<CartMedicineHolder> {

        List<CartMedicine> mCartMedicines;

        public CartMedicineAdapter(List<CartMedicine> cartMedicines) {
            mCartMedicines = cartMedicines;
        }

        @Override
        public CartMedicineHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view =layoutInflater.inflate(R.layout.list_item_medicine_cart, parent, false);

            return new CartMedicineHolder(view);
        }

        @Override
        public void onBindViewHolder(CartMedicineHolder holder, int position) {

            CartMedicine cartMedicine = mCartMedicines.get(position);
            holder.bindCartMedicine(cartMedicine);
        }

        @Override
        public int getItemCount() {
            return mCartMedicines.size();
        }

        public void updateList(List<CartMedicine> cartMedicines) {

            this.mCartMedicines = cartMedicines;
            notifyDataSetChanged();
        }
    }

    private void updatePrice() {
        List<CartMedicine> cartMedicines = mPrescriptionHandler.getPrescriptionCartMedicines();
        double totalPrice = 0;

        for (CartMedicine cartMedicine : cartMedicines) {
            totalPrice += MedicineLab.get(getActivity()).getMedicine(cartMedicine.getMedicineID()).getPrice() * cartMedicine.getQuantity();
        }

        mTotalPrice.setText(String.valueOf(totalPrice));
    }

}
