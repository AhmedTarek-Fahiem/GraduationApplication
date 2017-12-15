package com.example.ahmed_tarek.graduationapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Ahmed_Tarek on 17/11/23.
 */

public class MedicineListFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private RecyclerView mMedicineListRecyclerView;
    private MedicineAdapter mMedicineAdapter;

    private MedicineLab medicineLab;
    private CartLab mCartLab;

    private EditText mSearchTextEditText;
    private FloatingActionButton mSearchSubmitButton;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        medicineLab = MedicineLab.get();
        mCartLab = CartLab.get();

        if (savedInstanceState == null) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            editor.putBoolean("cartFlag", false);
            editor.apply();
            mCartLab.clearMedicines();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.navigation_drawer, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) view.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView = navigationView.getHeaderView(0);

        TextView username = (TextView) navigationHeaderView.findViewById(R.id.navigation_header_username);
        username.setText(Customer.getCustomer().getUsername());
        TextView email = (TextView) navigationHeaderView.findViewById(R.id.navigation_header_email);
        email.setText(Customer.getCustomer().getEMail());

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        mMedicineListRecyclerView = (RecyclerView) view.findViewById(R.id.medicine_list_recycler_view);
        mMedicineListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (mMedicineListRecyclerView.getAdapter() == null) {
            mMedicineAdapter = new MedicineAdapter(null);
            mMedicineListRecyclerView.setAdapter(mMedicineAdapter);
        }

        mSearchTextEditText = (EditText) view.findViewById(R.id.search_label);
        mSearchTextEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {

                if (mSearchTextEditText.getText().length() == 0) {
                    mMedicineAdapter.updateList(null);
                } else {
                    mMedicineAdapter.updateList(medicineLab.getMedicines(mSearchTextEditText.getText().toString()));
                }
            }
        });

        mSearchSubmitButton = (FloatingActionButton) view.findViewById(R.id.search_submit);
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("cartFlag", false)) {
            mSearchSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSearchSubmitButton.setVisibility(View.GONE);
        }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar_items, menu);
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Customer.getCustomer().getUsername(),false)) {
            menu.findItem(R.id.bar_recent_qr).setVisible(true);
        } else {
            menu.findItem(R.id.bar_recent_qr).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.bar_recent_qr :

                Intent intent = QRActivity.newIntent(getActivity());
                startActivity(intent);

                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.medical_history :

                //////in progress
                break;
            case R.id.order_history :

                //////in progress
                break;
            case R.id.sign_out :
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("isLogin", false).apply();
                Intent intent = new Intent(getActivity(), AccessActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private class MedicineHolder extends RecyclerView.ViewHolder {

        private Medicine mMedicineHold;

        private CheckBox mSelectedCheckBox;
        private TextView mMedicineNameTextView;
        private FloatingActionButton mDetailsButton;

        public MedicineHolder(View itemView) {
            super(itemView);

            mSelectedCheckBox = (CheckBox) itemView.findViewById(R.id.medicine_selected_check_box);
            mMedicineNameTextView = (TextView) itemView.findViewById(R.id.medicine_name);
            mDetailsButton = (FloatingActionButton) itemView.findViewById(R.id.medicine_details_button);

        }

        public void bindMedicine(Medicine medicine) {
            mMedicineHold = medicine;

            mMedicineNameTextView.setText(mMedicineHold.getName());
            mSelectedCheckBox.setChecked(mCartLab.isExist(mMedicineHold.getID()));
            if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("cartFlag", false)) {
                mSearchSubmitButton.setVisibility(View.VISIBLE);
            }

            mSelectedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                    if (compoundButton.isChecked()) {
                        mCartLab.addMedicine(mMedicineHold);
                        editor.putBoolean("cartFlag", true);
                        editor.apply();
                        mSearchSubmitButton.setVisibility(View.VISIBLE);
                    } else {
                        mCartLab.removeMedicine(mMedicineHold);
                        if (mCartLab.getCartMedicines().size() == 0) {
                            editor.putBoolean("cartFlag", false);
                            editor.apply();
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
