package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;

import java.text.NumberFormat;

public class ProductDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PRODUCT_DETAIL_LOADER = 2;

    private Uri selectedProductUri;

    private TextView nameInput;
    private TextView priceInput;
    private TextView quantityInput;
    private TextView supplierNameTv;
    private TextView supplierPhoneTv;
    private ImageView productImgView;
    private Button callSupplierBtn;

    private int quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        // Activity opens only with an intent containing
        // a product Uri
        Intent intent = getIntent();
        selectedProductUri = intent.getData();
        initFields();
        getSupportLoaderManager().initLoader(PRODUCT_DETAIL_LOADER, null, this);
    }

    /**
     * Prepare all fields for data
     */
    private void initFields() {
        nameInput = findViewById(R.id.product_name);
        priceInput = findViewById(R.id.product_price);
        quantityInput = findViewById(R.id.product_quantity);
        supplierNameTv = findViewById(R.id.supplier_name);
        supplierPhoneTv = findViewById(R.id.supplier_phone);
        callSupplierBtn = findViewById(R.id.call_btn);
        productImgView = findViewById(R.id.product_image);

        Button increaseBtn = findViewById(R.id.increase_btn);
        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, Integer.valueOf(quantityInput.getText().toString()) + 1);
                getContentResolver().update(selectedProductUri, contentValues, null, null);
            }
        });

        Button decreaseBtn = findViewById(R.id.decrease_btn);
        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    quantity--;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                    getContentResolver().update(selectedProductUri, contentValues, null, null);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedMenuItemId = item.getItemId();
        switch (selectedMenuItemId) {
            case R.id.action_edit:
                Intent editIntent = new Intent(this, PanelActivity.class);
                editIntent.setData(selectedProductUri);
                startActivity(editIntent);
                return true;
            case R.id.action_delete:
                showDeleteWarningDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProduct(selectedProductUri);
                        finish();
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteWarningDialog(DialogInterface.OnClickListener
                                                 confirmationButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_warning_dialog_msg);
        builder.setPositiveButton(R.string.yes, confirmationButtonClickListener);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct(Uri productUri) {
        getContentResolver().delete(productUri, null, null);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                InventoryContract.ProductEntry.COLUMN_PRODUCT_ID,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_THUMBNAIL,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE,
        };
        return new CursorLoader(this, selectedProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            String productThumbnailPath = cursor.getString(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_THUMBNAIL));
            Uri productThumbnailUri = Uri.parse(productThumbnailPath);
            productImgView.setImageURI(productThumbnailUri);

            String name = cursor.getString(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME));
            nameInput.setText(name);
            setTitle(name);

            int priceInCents = cursor.getInt(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE));
            double price = (double) priceInCents / 100;
            priceInput.setText(NumberFormat.getCurrencyInstance().format(price));

            quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
            quantityInput.setText(String.valueOf(quantity));

            String supplierName = cursor.getString(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME));
            supplierNameTv.setText(supplierName);

            final String supplierPhone = cursor.getString(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE));
            supplierPhoneTv.setText(supplierPhone);

            callSupplierBtn.setVisibility(View.VISIBLE);
            callSupplierBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callSupplier(supplierPhone);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // nothing
    }

    private void callSupplier(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        startActivity(intent);
    }
}
