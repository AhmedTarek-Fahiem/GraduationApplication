package com.example.ahmed_tarek.graduationapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private RecyclerView mCartListRecyclerView;
    private CartMedicineAdapter mCartMedicineAdapter;

    private CartLab cartLab;
    List<Medicine> medicines;

    private Button mGenerateButton;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cart_fragment, container, false);

        mCartListRecyclerView = (RecyclerView) view.findViewById(R.id.cart_list_recycler_view);
        mCartListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        cartLab = CartLab.get();
        medicines = cartLab.getCartMedicines();

        if (mCartMedicineAdapter == null) {
            mCartMedicineAdapter = new CartMedicineAdapter(medicines);
            mCartListRecyclerView.setAdapter(mCartMedicineAdapter);
        } else {
            mCartMedicineAdapter.notifyDataSetChanged();
        }

        mGenerateButton = (Button) view.findViewById(R.id.generate_qr);
        mGenerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                medicines.clear();
                medicines = cartLab.getCartMedicines();
                String cartMedicines = "";
                for(int i = 0 ; i < medicines.size() ; i++){
                    cartMedicines += medicines.get(i).getName();
                    cartMedicines += ',';
                    cartMedicines += String.valueOf(medicines.get(i).getQuantity());
                    if(i != (medicines.size() - 1))
                        cartMedicines += '&';
                }
                Log.d("QRSTRING", cartMedicines);
                Intent intent = new Intent(getActivity(), QRActivity.class);
                intent.putExtra("1", cartMedicines);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    private class CartMedicineHolder extends RecyclerView.ViewHolder {

        private Medicine mMedicine;

        private TextView mMedicineNameTextView;
        private EditText mMedicineQuantity;
        private Spinner mMedicineRepeat;

        public CartMedicineHolder(View itemView) {
            super(itemView);

            mMedicineNameTextView = (TextView) itemView.findViewById(R.id.cart_medicine_name);
            mMedicineQuantity = (EditText) itemView.findViewById(R.id.cart_medicine_quantity);
            mMedicineRepeat = (Spinner) itemView.findViewById(R.id.cart_medicine_regular_spinner);
        }

        public void bindMedicine(Medicine medicine) {
            mMedicine = medicine;

            mMedicineNameTextView.setText(mMedicine.getName());

            mMedicineQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                    if (count != 0) {
                        cartLab.setMedicineQuatity(mMedicine.getID(), Integer.parseInt(charSequence.toString()));
                    } else {
                        cartLab.setMedicineQuatity(mMedicine.getID(), 1);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });

            mMedicineRepeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    String selectedItem = adapterView.getItemAtPosition(position).toString();
                    if (selectedItem.equals("None")) {
                        cartLab.setMedicineRepeat(mMedicine.getID(), 0);
                    } else if (selectedItem.equals("Weekly")) {
                        cartLab.setMedicineRepeat(mMedicine.getID(), 7);
                    } else {
                        cartLab.setMedicineRepeat(mMedicine.getID(), 30);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

        }
    }

    private class CartMedicineAdapter extends RecyclerView.Adapter<CartMedicineHolder> {

        List<Medicine> mMedicines;

        public CartMedicineAdapter(List<Medicine> medicines) {
            mMedicines = medicines;
        }

        @Override
        public CartMedicineHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view =layoutInflater.inflate(R.layout.list_item_medicine_cart, parent, false);

            return new CartMedicineHolder(view);
        }

        @Override
        public void onBindViewHolder(CartMedicineHolder holder, int position) {

            Medicine medicine = mMedicines.get(position);
            holder.bindMedicine(medicine);
        }

        @Override
        public int getItemCount() {
            return mMedicines.size();
        }
    }

}
