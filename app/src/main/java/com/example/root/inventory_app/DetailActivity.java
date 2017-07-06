package com.example.root.inventory_app;

import android.content.ContentValues;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.inventory_app.data.ItemContract.ItemEntry;

public class DetailActivity extends AppCompatActivity {

    private Spinner mItemTypeSpinner;

    private Uri mCurrentItemUri;

    private Uri imageUri;
    private ImageView mItemImage;
    private EditText mItemName;
    private EditText mItemInfo;
    private EditText mItemSupplier;
    private EditText mItemPrice;
    private EditText mQuantity;
    private Button mQuantityDecrement;
    private Button mQuantityIncrement;
    private Button mOrderItem;

    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            return false;
        }
    };

    private int mType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get the associated URI
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.detail_activity_title_add_item));
        } else {
            setTitle(getString(R.string.detail_activity_title_edit_item));
        }

        // Find all relevant views to reed input from
        mItemImage = (ImageView) findViewById(R.id.item_picture_detail);
        mItemName = (EditText) findViewById(R.id.edit_item_name);
        mItemInfo = (EditText) findViewById(R.id.edit_item_information);
        mItemSupplier = (EditText) findViewById(R.id.edit_item_supplier);
        mItemPrice = (EditText) findViewById(R.id.edit_item_price);
        mQuantity = (EditText) findViewById(R.id.edit_item_quantity);
        mItemTypeSpinner = (Spinner) findViewById(R.id.spinner_item_type);
        mQuantityDecrement = (Button) findViewById(R.id.quantity_decrement);
        mQuantityIncrement = (Button) findViewById(R.id.quantity_increment);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modify them.
        mItemName.setOnTouchListener(mTouchListener);
        mItemInfo.setOnTouchListener(mTouchListener);
        mItemSupplier.setOnTouchListener(mTouchListener);
        mItemPrice.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mItemTypeSpinner.setOnTouchListener(mTouchListener);
        mQuantityDecrement.setOnTouchListener(mTouchListener);
        mQuantityIncrement.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    private void setupSpinner() {
        final ArrayAdapter itemTypeSpinner = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);

        itemTypeSpinner.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mItemTypeSpinner.setAdapter(itemTypeSpinner);
//        String selectedItemType = getResources().getStringArray(R.array.array_type_options)
//                [itemTypeSpinner.getPosition()];
        mItemTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemType = parent.getItemAtPosition(position).toString();
                if (!TextUtils.isEmpty(selectedItemType)) {
                    if (selectedItemType.equals(getString(R.string.spinner_other))) {
                        mType = ItemEntry.ITEM_TYPE_OTHER;
                    } else if (selectedItemType.equals(getString(R.string.spinner_sofas))) {
                        mType = ItemEntry.ITEM_TYPE_SOFAS;
                    } else if (selectedItemType.equals(getString(R.string.spinner_chairs))) {
                        mType = ItemEntry.ITEM_TYPE_CHAIRS;
                    } else if (selectedItemType.equals(getString(R.string.spinner_tables))) {
                        mType = ItemEntry.ITEM_TYPE_TABLES;
                    } else if (selectedItemType.equals(getString(R.string.spinner_beds))) {
                        mType = ItemEntry.ITEM_TYPE_BEDS;
                    } else if (selectedItemType.equals(getString(R.string.spinner_desks))) {
                        mType = ItemEntry.ITEM_TYPE_DESKS;
                    } else if (selectedItemType.equals(getString(R.string.spinner_cabinets))) {
                        mType = ItemEntry.ITEM_TYPE_CABINETS;
                    } else if (selectedItemType.equals(getString(R.string.spinner_wardrobes))) {
                        mType = ItemEntry.ITEM_TYPE_WARDROBES;
                    } else if (selectedItemType.equals(getString(R.string.spinner_textiles))) {
                        mType = ItemEntry.ITEM_TYPE_TEXTILES;
                    } else {
                        mType = ItemEntry.ITEM_TYPE_DECORATION;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = ItemEntry.ITEM_TYPE_OTHER;
            }
        });
    }

    private void saveNewItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String itemNameString = mItemName.getText().toString().trim();
        String itemInfoString = mItemInfo.getText().toString().trim();
        String itemSupplierString = mItemSupplier.getText().toString().trim();
        String itemQuantityString = mQuantity.getText().toString().trim();
        String itemPriceString = mItemPrice.getText().toString().trim();
        // ToDo string for quantity

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        // TODO add if the quantity is changed
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(itemNameString) && TextUtils.isEmpty(itemInfoString) &&
                TextUtils.isEmpty(itemSupplierString) && TextUtils.isEmpty(itemQuantityString) &&
                TextUtils.isEmpty(itemPriceString) && mType == ItemEntry.ITEM_TYPE_OTHER) {
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, itemNameString);
        values.put(ItemEntry.COLUMN_ITEM_INFORMATION, itemInfoString);
        values.put(ItemEntry.COLUMN_ITEM_TYPE, mType);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, itemSupplierString);

        int quantity = 0;
        if (!TextUtils.isEmpty(itemQuantityString)) {
            quantity = Integer.parseInt(itemQuantityString);
        }
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);

        double price = 0;
        if (!TextUtils.isEmpty(itemPriceString)) {
            price = Double.parseDouble(itemPriceString);
        }
        values.put(ItemEntry.COLUMN_ITEM_PRICE, price);

        if (mCurrentItemUri == null) {
            // This is a new item, so insert a new item into the provider.
            // Return the content URI for the new item.
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                Toast.makeText(this, "Error with saving the item",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item saved",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int mRowsUpdated = getContentResolver().update(
                    mCurrentItemUri,
                    values,
                    null,
                    null);
            if (mRowsUpdated == 0) {
                Toast.makeText(this, "Error with updating the item " + mRowsUpdated,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item updated",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_detail.xml file
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save the Item to the database
                saveNewItem();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Delete the Item
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
//            case android.R.id.home:
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
