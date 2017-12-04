package com.example.ahmed_tarek.graduationapplication;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/11/05.
 */

public class MedicineDetailsFragment extends Fragment {

    private static final String ARG_MEDICINE_ID = "medicine_id";

    private Medicine mMedicine;

    public static MedicineDetailsFragment newInstance(UUID medicineId) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_MEDICINE_ID, medicineId);

        MedicineDetailsFragment fragment = new MedicineDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID medicineId = (UUID) getArguments().getSerializable(ARG_MEDICINE_ID);
        mMedicine = MedicineLab.get().getMedicine(medicineId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        TextView mMedicineName;
        TextView mMedicineCategory;
        TextView mMedicineForm;
        TextView mMedicineConcentration;
        TextView mMedicineActiveIngredients;
        TextView mMedicinePrice;

        View view = inflater.inflate(R.layout.medicine_detailes, container, false);

        mMedicineName = (TextView) view.findViewById(R.id.details_medicine_name);
        mMedicineCategory = (TextView) view.findViewById(R.id.details_medicine_category);
        mMedicineForm = (TextView) view.findViewById(R.id.details_medicine_form);
        mMedicineConcentration = (TextView) view.findViewById(R.id.details_medicine_concentration);
        mMedicineActiveIngredients = (TextView) view.findViewById(R.id.details_medicine_active_ingredients);
        mMedicinePrice = (TextView) view.findViewById(R.id.details_medicine_price);

        mMedicineName.setText(mMedicine.getName());
        mMedicineCategory.setText(mMedicine.getCategory());
        mMedicineForm.setText(mMedicine.getForm());
        mMedicineConcentration.setText(String.valueOf(mMedicine.getConcentration()));
        mMedicineActiveIngredients.setText(mMedicine.getActiveIngredients());
        mMedicinePrice.setText(String.valueOf(mMedicine.getPrice()));

        return view;
    }
}
