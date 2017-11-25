package com.example.ahmed_tarek.graduationapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ahmed_Tarek on 17/11/23.
 */

public class CartListFragment extends Fragment {

    private RecyclerView mCartListRecyclerView;
    private CartMedicineAdapter mCartMedicineAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cart_fragment, container, false);

        mCartListRecyclerView = (RecyclerView) view.findViewById(R.id.cart_list_recycler_view);
        mCartListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        CartLab cartLab = CartLab.get();
        List<Medicine> medicines = cartLab.getCartMedicines();

        if (mCartMedicineAdapter == null) {
            mCartMedicineAdapter = new CartMedicineAdapter(medicines);
            mCartListRecyclerView.setAdapter(mCartMedicineAdapter);
        } else {
            mCartMedicineAdapter.notifyDataSetChanged();
        }

        return view;
    }

    private class CartMedicineHolder extends RecyclerView.ViewHolder {

        private Medicine mMedicine;

        private TextView mMedicineNameTextView;

        public CartMedicineHolder(View itemView) {
            super(itemView);

            mMedicineNameTextView = (TextView) itemView.findViewById(R.id.cart_medicine_name);
        }

        public void bindMedicine(Medicine medicine) {
            mMedicine = medicine;

            mMedicineNameTextView.setText(mMedicine.getName());
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
