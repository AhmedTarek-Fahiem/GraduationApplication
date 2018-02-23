package com.example.ahmed_tarek.graduationapplication;

import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/11/05.
 */

public class MedicineDetailsDialog extends DialogFragment {

    private static final String ARG_MEDICINE_ID = "medicine_id";

    private Medicine mMedicine;


    public static MedicineDetailsDialog newInstance(UUID medicineId) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_MEDICINE_ID, medicineId);

        MedicineDetailsDialog fragment = new MedicineDetailsDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMedicine = MedicineLab.get(getActivity()).getMedicine((UUID) getArguments().getSerializable(ARG_MEDICINE_ID));
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.medicine_details, container, false);

        getDialog().setTitle(mMedicine.getName());

        TextView medicineName = (TextView) view.findViewById(R.id.details_medicine_name);
        TextView medicineCategory = (TextView) view.findViewById(R.id.details_medicine_category);
        TextView medicineForm = (TextView) view.findViewById(R.id.details_medicine_form);
        TextView medicineActiveIngredients = (TextView) view.findViewById(R.id.details_medicine_active_ingredients);
        TextView medicinePrice = (TextView) view.findViewById(R.id.details_medicine_price);

        medicineName.setText(mMedicine.getName());
        medicineCategory.setText(mMedicine.getCategory());
        medicineForm.setText(mMedicine.getForm());
        medicineActiveIngredients.setText(mMedicine.getActiveIngredients());
        medicinePrice.setText(String.valueOf(mMedicine.getPrice()));

        return view;
    }
}
