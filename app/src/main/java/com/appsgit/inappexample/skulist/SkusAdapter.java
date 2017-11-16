package com.appsgit.inappexample.skulist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appsgit.inappexample.R;
import com.appsgit.inappexample.billing.BillingProvider;
import com.appsgit.inappexample.skulist.row.RowViewHolder;
import com.appsgit.inappexample.skulist.row.SkuRowData;

import java.util.List;

/**
 * Adapter for a RecyclerView that shows SKU details for the app.
 */
public class SkusAdapter extends RecyclerView.Adapter<RowViewHolder>
        implements RowViewHolder.OnButtonClickListener {
    private List<SkuRowData> mListData;
    private BillingProvider mBillingProvider;

    public SkusAdapter(BillingProvider billingProvider) {
        mBillingProvider = billingProvider;
    }

    void updateData(List<SkuRowData> data) {
        mListData = data;
        notifyDataSetChanged();
    }

    @Override
    public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sku_details_row, parent, false);
        return new RowViewHolder(item, this);
    }

    @Override
    public void onBindViewHolder(RowViewHolder holder, int position) {
        SkuRowData data = getData(position);
        if (data != null) {
            holder.title.setText(data.getTitle());
            holder.description.setText(data.getDescription());
            holder.button.setEnabled(true);
            holder.button.setText("BUY (" + data.getPrice() + ")");
        }
        switch (data.getSku()) {
            case "theme1":
                holder.skuIcon.setImageResource(R.drawable.theme1_icon);
                break;
            case "theme2":
                holder.skuIcon.setImageResource(R.drawable.theme2_icon);
                break;
            case "subscription_monthly":
                holder.skuIcon.setImageResource(R.drawable.monthly_subscription_icon);
                break;
            case "subscription_yearly":
                holder.skuIcon.setImageResource(R.drawable.yearly_subscription_icon);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mListData == null ? 0 : mListData.size();
    }

    @Override
    public void onButtonClicked(int position) {
        SkuRowData data = getData(position);
        mBillingProvider.getBillingManager().startPurchaseFlow(data.getSku(),
                data.getBillingType());

    }

    private SkuRowData getData(int position) {
        return mListData == null ? null : mListData.get(position);
    }
}

