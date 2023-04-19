package com.zybooks.stockmate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemHolder> {

    // Logcat tag
    private static final String TAG = "ItemAdapter";

    // Collection of inventory items in this list/adapter
    private final List<Item> mItems;

    // A context that this item adapter is running in
    private final Context mCtx;

    // An instance of the app's database
    InventoryDatabase inventoryDatabase;

    /**
     * Constructor that takes in a list of inventory items, an app context, and an instance
     * of the inventory database.
     *
     * @param items       Inventory items
     * @param ctx         A given app context
     * @param inventoryDb An instance of the inventory database
     */
    public ItemAdapter(List<Item> items, Context ctx, InventoryDatabase inventoryDb) {
        mItems = items;
        mCtx = ctx;
        inventoryDatabase = inventoryDb;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create an instance of the child view
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_items, parent, false);
        return new ItemHolder(view, inventoryDatabase);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        Item item = mItems.get(position);
        holder.bind(item);
        setActionsButtonClickListener(holder, item);
    }

    private void setActionsButtonClickListener(ItemHolder holder, Item item) {
        holder.mItemActionsBtn.setOnClickListener(v -> showActionsPopupMenu(holder, item));
    }

    private void showActionsPopupMenu(ItemHolder holder, Item item) {
        PopupMenu popup = new PopupMenu(mCtx, holder.mItemActionsBtn);
        popup.inflate(R.menu.inventory_item_actions_menu);
        popup.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_edit:
                    navigateToEditScreen(item);
                    return true;
                case R.id.menu_remove:
                    showConfirmationDialog(holder, item);
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

    private void navigateToEditScreen(Item item) {
        Intent intent = new Intent(mCtx, EditItemActivity.class);
        intent.putExtra(EditItemActivity.EXTRA_ITEM, item);
        mCtx.startActivity(intent);
    }

    private void showConfirmationDialog(ItemHolder holder, Item item) {
        new AlertDialog.Builder(mCtx)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton("Yes", (dialog, which) -> removeItem(holder, item))
                .setNegativeButton("No", null)
                .show();
    }

    private void removeItem(ItemHolder holder, Item item) {
        boolean deleted = inventoryDatabase.deleteItem(item);
        if (deleted) {
            mItems.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
            notifyDataSetChanged();
        } else {
            Toast.makeText(mCtx, R.string.delete_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Child view for each inventory item
     */
    static class ItemHolder extends RecyclerView.ViewHolder {

        // Cached view references
        private Item mItem;
        private final TextView mNameTextView;
        private final EditText mQuantityView;

        // Instance of the database
        InventoryDatabase inventoryDatabase;

        ImageButton mDecreaseQuantityBtnInline;
        ImageButton mIncreaseQuantityBtnInline;
        ImageButton mItemActionsBtn;

        /**
         * Constructor that requires the view and an instance of the database
         *
         * @param itemView    The view associated with this controller
         * @param inventoryDb Instance of the app database
         */
        public ItemHolder(View itemView, InventoryDatabase inventoryDb) {
            super(itemView);
            inventoryDatabase = inventoryDb;
            mNameTextView = itemView.findViewById(R.id.itemName);
            mQuantityView = itemView.findViewById(R.id.editQuantity);
            mDecreaseQuantityBtnInline = itemView.findViewById(R.id.decreaseQuantityBtnInline);
            mIncreaseQuantityBtnInline = itemView.findViewById(R.id.increaseQuantityBtnInline);
            mItemActionsBtn = itemView.findViewById(R.id.itemActionsBtn);

            // Update the item's quantity when the decrement button is clicked
            mDecreaseQuantityBtnInline.setOnClickListener(v -> {
                mItem.decrementQuantity();
                boolean updated = inventoryDatabase.updateItem(mItem);
                if (updated) {
                    mQuantityView.setText(String.valueOf(mItem.getQuantity()));
                }
            });

            // Update the items' quantity when the increment button is clicked
            mIncreaseQuantityBtnInline.setOnClickListener(v -> {
                mItem.incrementQuantity();
                boolean updated = inventoryDatabase.updateItem(mItem);
                if (updated) {
                    mQuantityView.setText(String.valueOf(mItem.getQuantity()));
                }
            });

            // Listen for changes on the quantity text field, updating the item and database
            mQuantityView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    mItem.setQuantity(getItemQuantity());
                    boolean updated = inventoryDatabase.updateItem(mItem);
                    Log.d(TAG, "Item quantity updated: " + updated);

                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        /**
         * Bind the model to the controller
         *
         * @param item Model for this view
         */
        public void bind(Item item) {
            mItem = item;
            Log.d("ItemHolder", "Bind item: " + mItem.getName());
            mNameTextView.setText(mItem.getName());
            mQuantityView.setText(String.valueOf(mItem.getQuantity()));
        }

        /**
         * Helper method to convert the quantity from the text input to an integer value
         *
         * @return Item's quantity
         */
        private int getItemQuantity() {
            String rawValue = mQuantityView.getText().toString().replaceAll("[^\\d.]", "").trim();
            int quantity = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);

            // Quantity cannot be less than 0
            return Math.max(quantity, 0);
        }
    }
}
