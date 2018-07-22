package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.android.inventoryapp.Adapters.ProductsAdapter;
import com.example.android.inventoryapp.data.InventoryContract.*;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private ProductsAdapter productsAdapter;
    private static final int PRODUCTS_LOADER_KEY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // setup the add button
        FloatingActionButton addBtn = findViewById(R.id.add_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PanelActivity.class);
                startActivity(intent);
            }
        });
        displayProducts();
    }

    /**
     * Populate ListView of products
     */
    private void displayProducts() {
        ListView productsList = findViewById(R.id.list);
        LinearLayout emptyView = findViewById(R.id.empty_stock);
        productsList.setEmptyView(emptyView);
        productsAdapter = new ProductsAdapter(this, null);
        productsList.setAdapter(productsAdapter);
        productsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(MainActivity.this, ProductDetailActivity.class);
                detailIntent.setData(ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id));
                startActivity(detailIntent);
            }
        });
        getSupportLoaderManager().initLoader(PRODUCTS_LOADER_KEY, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                ProductEntry.COLUMN_PRODUCT_ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_THUMBNAIL,
        };
        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        productsAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        productsAdapter.swapCursor(null);
    }
}
