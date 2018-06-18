package com.example.ahmed_tarek.graduationapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ahmed_Tarek on 17/12/14.
 */

public class OrderHistory extends Fragment {

    private DrawerInterface mDrawerInterface;
    private PrescriptionAdapter mPrescriptionAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mDrawerInterface = (DrawerInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement DrawerInterface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_history_fragment, container, false);

        mDrawerInterface.unlockDrawer();
        mDrawerInterface.checkedNavigationItem(1);

        RecyclerView orderHistoryRecyclerView = view.findViewById(R.id.order_history_list_recycler_view);
        orderHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (orderHistoryRecyclerView.getAdapter() == null) {
            mPrescriptionAdapter = new PrescriptionAdapter(PrescriptionLab.get(getActivity()).getPrescriptions(UserLab.get(getActivity()).getUserUUID()));
            orderHistoryRecyclerView.setAdapter(mPrescriptionAdapter);
        }

        if (mPrescriptionAdapter.getItemCount() > 0) {
            LinearLayout linearLayout = view.findViewById(R.id.order_empty_window);
            linearLayout.setVisibility(View.GONE);
        }

        return view;
    }

    private class PrescriptionHolder extends RecyclerView.ViewHolder {

        private TextView mPrescriptionDateTextView;
        private TextView mPrescriptionPriceTextView;
        private ImageButton mAddButton;

        public PrescriptionHolder(View itemView) {
            super(itemView);

            mPrescriptionDateTextView = itemView.findViewById(R.id.prescription_date);
            mPrescriptionPriceTextView = itemView.findViewById(R.id.prescription_price);
            mAddButton = itemView.findViewById(R.id.add_prescription);
        }

        public void bindOrder(final Prescription prescription) {

            mPrescriptionDateTextView.setText(new SimpleDateFormat("EEE dd MMM, yyyy - h:m a", Locale.ENGLISH).format(prescription.getDate()));
            mPrescriptionPriceTextView.setText("Price : " + String.valueOf(prescription.getPrice()) + " L.E");
            mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PrescriptionHandler.get().reset();
                    PrescriptionHandler prescriptionHandler = PrescriptionHandler.get();

                    List<CartMedicine> cartMedicines;
                    cartMedicines = PrescriptionLab.get(getActivity()).getCarts(prescription.getID());
                    for (CartMedicine cartMedicine : cartMedicines) {
                        prescriptionHandler.addCart(cartMedicine);
                    }

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String content = "";
                    List<CartMedicine> cartMedicines;
                    cartMedicines = PrescriptionLab.get(getActivity()).getCarts(prescription.getID());

                    for(int i = 0 ; i < cartMedicines.size() ; i++){
                        content = content.concat(MedicineLab.get(getActivity()).getMedicine(cartMedicines.get(i).getMedicineID()).getName());
                        content += '#';
                        content += String.valueOf(cartMedicines.get(i).getQuantity());
                        if(i != (cartMedicines.size() - 1))
                            content += '|';
                    }

                    QRActivity.MyDialogFragment.newInstance(content, null).show(getFragmentManager(), "prescription_dialog");
                }
            };
            mPrescriptionDateTextView.setOnClickListener(onClickListener);
            mPrescriptionPriceTextView.setOnClickListener(onClickListener);
        }
    }

    private class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionHolder> {

        List<Prescription> mPrescriptionList;

        public PrescriptionAdapter(List<Prescription> prescriptionList) {
            mPrescriptionList = prescriptionList;
        }

        @Override
        public PrescriptionHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_order_history, parent, false);

            return new PrescriptionHolder(view);
        }

        @Override
        public void onBindViewHolder(PrescriptionHolder holder, int position) {

            Prescription prescription = mPrescriptionList.get(position);
            holder.bindOrder(prescription);
        }

        @Override
        public int getItemCount() {

            if (mPrescriptionList == null) {
                return 0;
            }
            return mPrescriptionList.size();
        }

        public void updateList(List<Prescription> prescriptionList) {
            this.mPrescriptionList = prescriptionList;
            notifyDataSetChanged();
        }
    }
}
