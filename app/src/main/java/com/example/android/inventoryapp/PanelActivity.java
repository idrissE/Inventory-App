package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PanelActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int REQUEST_IMAGE_CAPTURE = 999;
    private static final int PRODUCT_EDIT_LOADER_KEY = 3;

    private EditText nameInput;
    private EditText priceInput;
    private EditText quantityInput;
    private EditText supplierNameInput;
    private EditText supplierPhoneInput;
    private ImageButton productImgView;
    private Button callSupplierBtn;

    private Uri selectedProductUri;
    private Uri productThumbnailUri;

    private boolean isProductTouched;
    private String pictureFilePath;

    private final View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            isProductTouched = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        nameInput = findViewById(R.id.product_name);
        priceInput = findViewById(R.id.price_label);
        quantityInput = findViewById(R.id.product_quantity);
        supplierNameInput = findViewById(R.id.supplier_name);
        supplierPhoneInput = findViewById(R.id.supplier_phone);
        callSupplierBtn = findViewById(R.id.call_btn);
        // setup for taking picture of the product
        productImgView = findViewById(R.id.product_image);
        productImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePicture();
            }
        });

        nameInput.setOnTouchListener(touchListener);
        priceInput.setOnTouchListener(touchListener);
        quantityInput.setOnTouchListener(touchListener);
        supplierNameInput.setOnTouchListener(touchListener);
        supplierPhoneInput.setOnTouchListener(touchListener);

        Intent intent = getIntent();
        if (intent.getData() != null) {
            // edit mode
            selectedProductUri = intent.getData();
            setTitle(getString(R.string.edit_title));
            invalidateOptionsMenu();
            getSupportLoaderManager().initLoader(PRODUCT_EDIT_LOADER_KEY, null, this);
        } else {
            // create mode
            setTitle(getString(R.string.add_title));
            invalidateOptionsMenu();
        }
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
            productThumbnailUri = Uri.parse(productThumbnailPath);
            pictureFilePath = productThumbnailPath;
            productImgView.setImageURI(productThumbnailUri);

            String name = cursor.getString(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME));
            nameInput.setText(name);

            int priceInCents = cursor.getInt(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE));
            double price = (double) priceInCents / 100;
            priceInput.setText(String.valueOf(price));

            int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
            quantityInput.setText(String.valueOf(quantity));

            String supplierName = cursor.getString(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME));
            supplierNameInput.setText(supplierName);

            final String supplierPhone = cursor.getString(cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE));
            supplierPhoneInput.setText(supplierPhone);

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.panel_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedMenuItemId = item.getItemId();
        switch (selectedMenuItemId) {
            case R.id.action_save_product:
                saveProduct();
                return true;
            case R.id.action_delete:
                showDeleteWarningDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProduct(selectedProductUri);
                        Intent intent = new Intent(PanelActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
                return true;
            case android.R.id.home:
                if (!isProductTouched) {
                    NavUtils.navigateUpFromSameTask(PanelActivity.this);
                    return true;
                }
                showUnsavedChangesDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(PanelActivity.this);
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (selectedProductUri == null) {
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            deleteMenuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Create a file to store the product image
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "inventoryApp" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePicture() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File pictureFile;
            try {
                pictureFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileProvider",
                        pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new File(pictureFilePath);
            if (imgFile.exists()) {
                productImgView.setImageURI(Uri.fromFile(imgFile));
                productThumbnailUri = Uri.fromFile(imgFile);
            }
        } else {
            Toast.makeText(this, getString(R.string.product_photo_error), Toast.LENGTH_LONG).show();
        }
    }

    private void saveProduct() {
        // thumbnail NOT NULL
        if (productThumbnailUri == null) {
            Toast.makeText(this, getString(R.string.no_thumbnail_error), Toast.LENGTH_LONG).show();
            return;
        }
        // name NOT NULL
        EditText nameTv = findViewById(R.id.product_name);
        String name = nameTv.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            nameTv.setError(getString(R.string.no_name_error));
        }
        // price NOT NULL
        EditText priceTv = findViewById(R.id.price_label);
        if (TextUtils.isEmpty(priceTv.getText().toString().trim())) {
            priceTv.setError(getString(R.string.no_price_error));
            return;
        }
        double price = Double.parseDouble(priceTv.getText().toString().trim());
        // quantity NOT NULL
        EditText quantityTv = findViewById(R.id.product_quantity);
        if (TextUtils.isEmpty(quantityTv.getText().toString().trim())) {
            quantityTv.setError(getString(R.string.no_quantity_error));
            return;
        }
        int quantity = Integer.parseInt(quantityTv.getText().toString().trim());

        // NOT NULL
        EditText supplierNameTv = findViewById(R.id.supplier_name);
        String supplierName = supplierNameTv.getText().toString().trim();
        if (TextUtils.isEmpty(supplierName)) {
            supplierNameTv.setError(getString(R.string.no_supplier_name_error));
            return;
        }
        // phone
        EditText supplierPhoneTv = findViewById(R.id.supplier_phone);
        String supplierPhone = supplierPhoneTv.getText().toString().trim();
        if (TextUtils.isEmpty(supplierPhone)) {
            supplierPhoneTv.setError(getString(R.string.no_supplier_phone_error));
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_THUMBNAIL, productThumbnailUri.toString());
        contentValues.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME, name);
        contentValues.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE, price * 100);
        contentValues.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        contentValues.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierName);
        contentValues.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supplierPhone);
        if (selectedProductUri == null) {
            getContentResolver().insert(InventoryContract.ProductEntry.CONTENT_URI, contentValues);
            Toast.makeText(this, getString(R.string.product_add_success), Toast.LENGTH_SHORT).show();
        } else {
            getContentResolver().update(selectedProductUri, contentValues, null, null);
            Toast.makeText(this, getString(R.string.product_update_success), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener
                                                  discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

    private void callSupplier(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        startActivity(intent);
    }

    private void deleteProduct(Uri productUri) {
        getContentResolver().delete(productUri, null, null);
    }
}
