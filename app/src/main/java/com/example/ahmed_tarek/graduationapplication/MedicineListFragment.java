package com.example.ahmed_tarek.graduationapplication;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Ahmed_Tarek on 17/11/23.
 */

public class MedicineListFragment extends Fragment {

    private static final String KEY_MEDICINE_SEARCH_TEXT = "search_text";

    private static Parcelable listState;

    private RecyclerView mMedicineListRecyclerView;
    private MedicineAdapter mMedicineAdapter;

    private MedicineLab medicineLab;
    private CartLab mCartLab;

    private EditText mSearchTextEditText;
    private Button mSearchSubmitButton;

    private CharSequence searchText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        medicineLab = MedicineLab.get();
        mCartLab = CartLab.get();

        if (savedInstanceState == null) {
            mCartLab.clearMedicines();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);


        mMedicineListRecyclerView = (RecyclerView) view.findViewById(R.id.medicine_list_recycler_view);
        mMedicineListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        mSearchTextEditText = (EditText) view.findViewById(R.id.search_label);
        mSearchTextEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                searchText = charSequence;

                updateUI();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mSearchSubmitButton = (Button) view.findViewById(R.id.search_submit);
        mSearchSubmitButton.setVisibility(View.GONE);
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

        return view;
    }

    private void updateUI() {

        List<Medicine> medicineList;
        medicineList = medicineLab.getMedicines(searchText);

        if (mMedicineListRecyclerView.getAdapter() == null) {
            mMedicineAdapter = new MedicineAdapter(medicineList);
            mMedicineListRecyclerView.setAdapter(mMedicineAdapter);
        } else {
            mMedicineAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(KEY_MEDICINE_SEARCH_TEXT, searchText);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getCharSequence(KEY_MEDICINE_SEARCH_TEXT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (searchText != null) {
            updateUI();
        }
    }

    private class MedicineHolder extends RecyclerView.ViewHolder {

        private Medicine mMedicineHold;

        private CheckBox mSelectedCheckBox;
        private TextView mMedicineNameTextView;
        private Button mDetailsButton;

        public MedicineHolder(View itemView) {
            super(itemView);

            mSelectedCheckBox = (CheckBox) itemView.findViewById(R.id.medicine_selected_check_box);
            mMedicineNameTextView = (TextView) itemView.findViewById(R.id.medicine_name);
            mDetailsButton = (Button) itemView.findViewById(R.id.medicine_details_button);

        }

        public void bindMedicine(Medicine medicine) {
            mMedicineHold = medicine;

            mMedicineNameTextView.setText(mMedicineHold.getName());
            mSelectedCheckBox.setChecked(mCartLab.isExist(mMedicineHold.getID()));

            mSelectedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (compoundButton.isChecked()) {
                        mCartLab.addMedicine(mMedicineHold);
                        mSearchSubmitButton.setVisibility(View.VISIBLE);
                    } else {
                        mCartLab.removeMedicine(mMedicineHold);
                        if (mCartLab.getCartMedicines().size() == 0) {
                            mSearchSubmitButton.setVisibility(View.GONE);
                        }
                    }
                }
            });

            mDetailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fragmentManager = getFragmentManager();
                    MedicineDetailsFragment medicineDetailsFragment = MedicineDetailsFragment.newInstance(mMedicineHold.getID());

                    fragmentManager.beginTransaction()
                            .replace(R.id.main_fragment_container, medicineDetailsFragment)
                            .addToBackStack(null)
                            .commit();
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
                return mMedicines.size();
        }

    }
}
