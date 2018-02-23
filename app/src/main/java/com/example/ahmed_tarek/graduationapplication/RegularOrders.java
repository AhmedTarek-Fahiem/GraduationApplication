package com.example.ahmed_tarek.graduationapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ahmed_tarek.graduationapplication.receivers.BootUpReceiver;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ahmed_Tarek on 17/12/14.
 */

public class RegularOrders extends Fragment {

    private DrawerInterface mDrawerInterface;
    private RegularOrderAdapter mRegularOrderAdapter;

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
        View view = inflater.inflate(R.layout.regular_order_fragment, container, false);

        mDrawerInterface.unlockDrawer();
        mDrawerInterface.checkedNavigationItem(2);

        RecyclerView regularOrderRecyclerView = (RecyclerView) view.findViewById(R.id.regular_order_list_recycler_view);
        regularOrderRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (regularOrderRecyclerView.getAdapter() == null) {
            mRegularOrderAdapter = new RegularOrderAdapter(RegularOrderLab.get(getActivity()).getRegularOrders());
            regularOrderRecyclerView.setAdapter(mRegularOrderAdapter);
        }

        return view;
    }

    private class RegularOrderHolder extends RecyclerView.ViewHolder {

        private Regular mRegularOrder;
        private TextView mOrderDateTextView;
        private ImageButton mRemoveAlarmButton;

        public RegularOrderHolder(View itemView) {
            super(itemView);

            mOrderDateTextView = (TextView) itemView.findViewById(R.id.regular_order_date);
            mRemoveAlarmButton = (ImageButton) itemView.findViewById(R.id.remove_regular_order);
        }

        public void bindOrder(final Regular regularOrder) {

            mRegularOrder = regularOrder;
            mOrderDateTextView.setText(new SimpleDateFormat("EEE dd MMM, yyyy - h:m a", Locale.ENGLISH).format(mRegularOrder.getTimeStamp()));
            mRemoveAlarmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!RegularOrderLab.get(getActivity()).removeEntry(mRegularOrder.getPrescriptionUUID(), mRegularOrder.getTimeStamp())) {
                        BootUpReceiver.cancelAlarm(getActivity(), mRegularOrder.getTimeStamp());
                    }
                    mRegularOrderAdapter.updateList(RegularOrderLab.get(getActivity()).getRegularOrders());
                }
            });

            mOrderDateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String content = "";
                    List<CartMedicine> cartMedicines;
                    cartMedicines = PrescriptionLab.get(getActivity()).getCarts(mRegularOrder.getPrescriptionUUID(), mRegularOrder.getTimeStamp());

                    for (int i = 0; i < cartMedicines.size(); i++) {
                        content += MedicineLab.get(getActivity()).getMedicine(cartMedicines.get(i).getMedicineID()).getName();
                        content += ',';
                        content += String.valueOf(cartMedicines.get(i).getQuantity());
                        if (i != (cartMedicines.size() - 1))
                            content += '&';
                    }

                    QRActivity.MyDialogFragment.newInstance(content).show(getFragmentManager(), "prescription_dialog");
                }
            });
        }

    }

    private class RegularOrderAdapter extends RecyclerView.Adapter<RegularOrderHolder> {

        List<Regular> mRegularOrderList;

        public RegularOrderAdapter(List<Regular> regularOrdersList) {
            mRegularOrderList = regularOrdersList;
        }

        @Override
        public RegularOrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_regular_order, parent, false);

            return new RegularOrderHolder(view);
        }

        @Override
        public void onBindViewHolder(RegularOrderHolder holder, int position) {

            Regular regularOrder = mRegularOrderList.get(position);
            holder.bindOrder(regularOrder);
        }

        @Override
        public int getItemCount() {

            if (mRegularOrderList == null) {
                return 0;
            }
            return mRegularOrderList.size();
        }

        public void updateList(List<Regular> regularOrderList) {
            this.mRegularOrderList = regularOrderList;
            notifyDataSetChanged();
        }
    }
}
