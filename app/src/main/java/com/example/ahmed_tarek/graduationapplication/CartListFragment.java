package com.example.ahmed_tarek.graduationapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Ahmed_Tarek on 17/11/23.
 */

public class CartListFragment extends Fragment {

    private DrawerInterface mDrawerInterface;
    private RecyclerView mCartListRecyclerView;
    private CartMedicineAdapter mCartMedicineAdapter;
    private Button mGenerateButton;
    private TextView mTotalPrice;

    private PrescriptionHandler mPrescriptionHandler;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cart_fragment, container, false);

        mDrawerInterface.lockDrawer();
        setHasOptionsMenu(false);

        mCartListRecyclerView = (RecyclerView) view.findViewById(R.id.cart_list_recycler_view);
        mCartListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mPrescriptionHandler = PrescriptionHandler.get();

        if (mCartMedicineAdapter == null) {
            mCartMedicineAdapter = new CartMedicineAdapter(mPrescriptionHandler.getPrescriptionCartMedicines());
            mCartListRecyclerView.setAdapter(mCartMedicineAdapter);
        } else {
            mCartMedicineAdapter.notifyDataSetChanged();
        }

        mTotalPrice = (TextView) view.findViewById(R.id.total_payment_number);

        mGenerateButton = (Button) view.findViewById(R.id.generate_qr);
        mGenerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<CartMedicine> qrMedicines;
                qrMedicines = mPrescriptionHandler.getPrescriptionCartMedicines();

                String cartMedicines = "";

                for(int i = 0 ; i < qrMedicines.size() ; i++){
                    cartMedicines += MedicineLab.get(getActivity()).getMedicine(qrMedicines.get(i).getMedicineID()).getName();///
                    cartMedicines += ',';
                    cartMedicines += String.valueOf(qrMedicines.get(i).getQuantity());
                    if(i != (qrMedicines.size() - 1))
                        cartMedicines += '&';
                }

                mPrescriptionHandler.setPrescriptionPrice(Double.parseDouble(mTotalPrice.getText().toString()));
                mPrescriptionHandler.prescriptionCommit(getActivity());

                Intent intent = QRActivity.newIntent(getActivity(), cartMedicines);
                startActivity(intent);
            }
        });

        updatePrice();

        return view;
    }

    private class CartMedicineHolder extends RecyclerView.ViewHolder {

        private CartMedicine mCartMedicine;

        private TextView mCartMedicineNameTextView;
        private EditText mCartMedicineQuantity;
        private Spinner mCartMedicineRepeat;

        public CartMedicineHolder(View itemView) {
            super(itemView);

            mCartMedicineNameTextView = (TextView) itemView.findViewById(R.id.cart_medicine_name);
            mCartMedicineQuantity = (EditText) itemView.findViewById(R.id.cart_medicine_quantity);
            mCartMedicineRepeat = (Spinner) itemView.findViewById(R.id.cart_medicine_regular_spinner);
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

                    if (count != 0) {
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
                    if (selectedItem.equals("None")) {
                        mPrescriptionHandler.setCartMedicineRepeatDuration(mCartMedicine.getMedicineID(), 0);
                    } else if (selectedItem.equals("Weekly")) {
                        mPrescriptionHandler.setCartMedicineRepeatDuration(mCartMedicine.getMedicineID(), 7);
                    } else {
                        mPrescriptionHandler.setCartMedicineRepeatDuration(mCartMedicine.getMedicineID(), 30);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
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
