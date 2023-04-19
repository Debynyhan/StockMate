package com.zybooks.stockmate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ConfirmationDialog implements ConfirmationDialogInterface {
    private final Context mContext;
    private final Item mItem;
    private final ItemRemoverInterface mItemRemover;

    public ConfirmationDialog(Context context, Item item, ItemRemoverInterface itemRemover) {
        mContext = context;
        mItem = item;
        mItemRemover = itemRemover;
    }

    @Override
    public void showDialog() {
        new AlertDialog.Builder(mContext)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mItemRemover.removeItem();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}