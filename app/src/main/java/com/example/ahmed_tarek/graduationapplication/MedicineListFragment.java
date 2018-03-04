package com.example.ahmed_tarek.graduationapplication;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Ahmed_Tarek on 17/11/23.
 */

public class MedicineListFragment extends Fragment {

    private DrawerInterface mDrawerInterface;
    private RecyclerView mMedicineListRecyclerView;
    private MedicineAdapter mMedicineAdapter;
    private EditText mSearchTextEditText;
    private FloatingActionButton mSearchSubmitButton;
    private TextView mMedicinesNumberTextView;

    private PrescriptionHandler mPrescriptionHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mDrawerInterface = (DrawerInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement DrawerInterface");
        }

        mPrescriptionHandler = PrescriptionHandler.get();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        mDrawerInterface.unlockDrawer();
        mDrawerInterface.checkedNavigationItem(0);
        setHasOptionsMenu(true);

        mMedicineListRecyclerView = view.findViewById(R.id.medicine_list_recycler_view);
        mMedicineListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (mMedicineListRecyclerView.getAdapter() == null) {
            mMedicineAdapter = new MedicineAdapter(null);
            mMedicineListRecyclerView.setAdapter(mMedicineAdapter);
        }

        mSearchTextEditText = view.findViewById(R.id.search_label);
        mSearchTextEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                submitButtonVisibility();
                if (mSearchTextEditText.getText().length() == 0) {
                    ((RelativeLayout.LayoutParams) mSearchTextEditText.getLayoutParams()).addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                    mMedicineAdapter.updateList(null);
                } else {
                    ((RelativeLayout.LayoutParams) mSearchTextEditText.getLayoutParams()).removeRule(RelativeLayout.CENTER_VERTICAL);
                    mMedicineAdapter.updateList(MedicineLab.get(getActivity()).getMedicines(mSearchTextEditText.getText().toString()));
                }
            }
        });

        mSearchSubmitButton = view.findViewById(R.id.search_submit);
        mSearchSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, new CartListFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        mMedicinesNumberTextView = view.findViewById(R.id.medicines_number);

        submitButtonVisibility();

        return view;
    }

    private void submitButtonVisibility() {
        if (!mPrescriptionHandler.isEmpty()) {
            mSearchSubmitButton.setVisibility(View.VISIBLE);
            mMedicinesNumberTextView.setVisibility(View.VISIBLE);
            mMedicinesNumberTextView.setText(String.valueOf(mPrescriptionHandler.getMedicinesNumber()));
        } else {
            mSearchSubmitButton.setVisibility(View.GONE);
            mMedicinesNumberTextView.setVisibility(View.GONE);
        }
    }

    private class MedicineHolder extends RecyclerView.ViewHolder {

        private Medicine mMedicineHold;

        private CheckBox mSelectedCheckBox;
        private TextView mMedicineNameTextView;
        private FloatingActionButton mDetailsButton;

        public MedicineHolder(View itemView) {
            super(itemView);

            mSelectedCheckBox = itemView.findViewById(R.id.medicine_selected_check_box);
            mMedicineNameTextView = itemView.findViewById(R.id.medicine_name);
            mDetailsButton = itemView.findViewById(R.id.medicine_details_button);

        }

        public void bindMedicine(Medicine medicine) {
            mMedicineHold = medicine;

            mMedicineNameTextView.setText(mMedicineHold.getName());
            mSelectedCheckBox.setChecked(mPrescriptionHandler.isExist(mMedicineHold.getID()));
            submitButtonVisibility();

            mSelectedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    if (compoundButton.isChecked()) {
                        mPrescriptionHandler.addCart(new CartMedicine(mMedicineHold.getID()));
                        submitButtonVisibility();
                    } else {
                        mPrescriptionHandler.removeCart(mMedicineHold);
                        submitButtonVisibility();
                    }
                }
            });

            mDetailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MedicineDetailsDialog.newInstance(mMedicineHold.getID()).show(getFragmentManager(), "medicine_details");
                }
            });
        }

    }

    private class MedicineAdapter extends RecyclerView.Adapter<MedicineHolder> {

        List<Medicine> mMedicines;

        public MedicineAdapter(List<Medicine> medicines) {
            mMedicines = medicines;
        }

        @Override
        public MedicineHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_medicine_search, parent, false);

            return new MedicineHolder(view);
        }

        @Override
        public void onBindViewHolder(MedicineHolder holder, int position) {

            Medicine medicine = mMedicines.get(position);
            holder.bindMedicine(medicine);
        }

        @Override
        public int getItemCount() {

            if (mMedicines == null) {
                return 0;
            }
            return mMedicines.size();
        }

        public void updateList(List<Medicine> medicines) {

            this.mMedicines = medicines;
            notifyDataSetChanged();
        }

    }
}
