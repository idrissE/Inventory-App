package com.example.android.inventoryapp.Adapters;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract;

import java.text.NumberFormat;

public class ProductsAdapter extends CursorAdapter {
    public ProductsAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final String name = cursor.getString(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME));
        TextView nameTv = view.findViewById(R.id.product_name);
        nameTv.setText(name);

        int priceInCents = cursor.getInt(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE));
        final double price = (double) priceInCents / 100;
        TextView priceTv = view.findViewById(R.id.product_price);
        priceTv.setText(NumberFormat.getCurrencyInstance().format(price));

        final int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
        TextView quantityTv = view.findViewById(R.id.product_quantity);
        quantityTv.setText(context.getString(R.string.in_stock, quantity));

        final String productThumbnailPath = cursor.getString(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_THUMBNAIL));
        Uri productThumbnailUri = Uri.parse(productThumbnailPath);
        ImageView productImg = view.findViewById(R.id.product_thumbnail);
        productImg.setImageURI(productThumbnailUri);

        final String id = cursor.getString(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_ID));

        Button saleBtn = view.findViewById(R.id.sale_btn);
        saleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.ProductEntry.CONTENT_URI, Long.parseLong(id));
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity - 1);
                    context.getContentResolver().update(currentProductUri, contentValues, null, null);
                    swapCursor(cursor);
                } else {
                    Toast.makeText(context, context.getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}