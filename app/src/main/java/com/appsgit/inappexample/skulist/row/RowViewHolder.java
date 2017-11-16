package com.appsgit.inappexample.skulist.row;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appsgit.inappexample.R;

/**
 * ViewHolder for quick access to row's views
 */
public final class RowViewHolder extends RecyclerView.ViewHolder {
    public TextView title, description;
    public Button button;
    public ImageView skuIcon;

    /**
     * Handler for a button click on particular row
     */
    public interface OnButtonClickListener {
        void onButtonClicked(int position);
    }

    public RowViewHolder(final View itemView, final OnButtonClickListener clickListener) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.title);
        description = (TextView) itemView.findViewById(R.id.description);
        skuIcon = (ImageView) itemView.findViewById(R.id.sku_icon);
        button = (Button) itemView.findViewById(R.id.state_button);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onButtonClicked(getAdapterPosition());
                }
            });
        }
    }
}
