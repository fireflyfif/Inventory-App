package com.example.root.inventory_app;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int MY_PERMISSIONS_REQUEST = 2;

    private static final int EXISTING_ITEM_LOADER = 0;

    private Spinner mItemTypeSpinner;

    private Uri mCurrentItemUri;

    private Uri mImageUri;
    private ImageView mItemImage;
    private EditText mItemName;
    private EditText mItemInfo;
    private EditText mItemSupplier;
    private EditText mItemPrice;
    private EditText mEditQuantity;
    private Button mQuantityDecrement;
    private Button mQuantityIncrement;
    private Button mOrderItem;
    private int quantity;
    private int mType = 0;

    private boolean mItemHasChanged = false;
    private boolean itemCanBeSaved = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanged = true;
            return false;
        }
    };

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
            getSupportLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant views to reed input from
        mItemImage = (ImageView) findViewById(R.id.item_picture_detail);
        mItemName = (EditText) findViewById(R.id.edit_item_name);
        mItemInfo = (EditText) findViewById(R.id.edit_item_information);
        mItemSupplier = (EditText) findViewById(R.id.edit_item_supplier);
        mItemPrice = (EditText) findViewById(R.id.edit_item_price);
        mEditQuantity = (EditText) findViewById(R.id.edit_item_quantity);
        mItemTypeSpinner = (Spinner) findViewById(R.id.spinner_item_type);
        mQuantityDecrement = (Button) findViewById(R.id.quantity_decrement);
        mQuantityIncrement = (Button) findViewById(R.id.quantity_increment);
        mOrderItem = (Button) findViewById(R.id.order_item_button);

        mItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Invoke method for opening an image folder
                requestPermissions();
                mItemHasChanged = true;
            }
        });

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modify them.
        mItemName.setOnTouchListener(mTouchListener);
        mItemInfo.setOnTouchListener(mTouchListener);
        mItemSupplier.setOnTouchListener(mTouchListener);
        mItemPrice.setOnTouchListener(mTouchListener);
        mEditQuantity.setOnTouchListener(mTouchListener);
        mItemTypeSpinner.setOnTouchListener(mTouchListener);
        mQuantityDecrement.setOnTouchListener(mTouchListener);
        mQuantityIncrement.setOnTouchListener(mTouchListener);

        setupSpinner();
        decrementQuantity();
        incrementQuantity();
        orderItem();
    }

    private void decrementQuantity() {
        mQuantityDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditQuantity.getText().toString().equals(null) ||
                        mEditQuantity.getText().toString().equals("")) {
                    Toast.makeText(DetailActivity.this, getString(R.string.quantity_no_value),
                            Toast.LENGTH_SHORT).show();
                } else if (quantity < 2) {
                    Toast.makeText(DetailActivity.this, getString(R.string.quantity_no_negative),
                            Toast.LENGTH_SHORT).show();
                } else {
                    quantity = Integer.parseInt(mEditQuantity.getText().toString());
                    mEditQuantity.setText(String.valueOf(quantity - 1));
                }
            }
        });
    }

    private void incrementQuantity() {
        mQuantityIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditQuantity.getText().toString().equals(null) ||
                        mEditQuantity.getText().toString().equals("")) {
                    Toast.makeText(DetailActivity.this, getString(R.string.quantity_no_value),
                            Toast.LENGTH_SHORT).show();
                } else if (quantity > 997) {
                    Toast.makeText(DetailActivity.this, getString(R.string.quantity_limit),
                            Toast.LENGTH_SHORT).show();
                } else {
                    quantity = Integer.parseInt(mEditQuantity.getText().toString());
                    mEditQuantity.setText(String.valueOf(quantity + 1));
                }
            }
        });
    }

    private void orderItem() {
        mOrderItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendEmailIntent = new Intent(Intent.ACTION_SENDTO);
                sendEmailIntent.setData(Uri.parse(getString(R.string.order_supplier_email)));
                sendEmailIntent.putExtra(Intent.EXTRA_EMAIL,
                        getString(R.string.order_extra_supplier_email));
                if (sendEmailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendEmailIntent);
                }
            }
        });
    }

    private void setupSpinner() {
        final ArrayAdapter itemTypeSpinner = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);

        itemTypeSpinner.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mItemTypeSpinner.setAdapter(itemTypeSpinner);

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

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
            }
        } else {
            mItemImage.setEnabled(true);
            openImageSelector();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                Log.v(LOG_TAG, "Uri: " + mImageUri);

                mItemImage.setImageURI(mImageUri);
                mItemImage.setImageBitmap(getBitmapFromUri(mImageUri));
                mItemImage.invalidate();
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        if (uri == null || uri.toString().isEmpty()) {
            return null;
        }

        // Get the dimensions of the View
        int targetWidth = mItemImage.getWidth();
        int targetHeight = mItemImage.getHeight();

        InputStream inputStream = null;
        try {
            inputStream = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, bmOptions);
            inputStream.close();

            int photoWidth = bmOptions.outWidth;
            int photoHeight = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoWidth / targetWidth, photoHeight / targetHeight);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            inputStream = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions);
            inputStream.close();
            return bitmap;

        } catch (FileNotFoundException noFile) {
            Log.e(LOG_TAG, "Failed to load image.", noFile);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException ioe) {
            }
        }
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private boolean saveNewItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String itemNameString = mItemName.getText().toString().trim();
        String itemInfoString = mItemInfo.getText().toString().trim();
        String itemSupplierString = mItemSupplier.getText().toString().trim();
        String itemQuantityString = mEditQuantity.getText().toString().trim();
        String itemPriceString = mItemPrice.getText().toString().trim();

        // Check if this is supposed to be a new item
        // and check if all the fields in the editor are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(itemNameString) &&
                TextUtils.isEmpty(itemInfoString) &&
                TextUtils.isEmpty(itemSupplierString) &&
                TextUtils.isEmpty(itemQuantityString) &&
                TextUtils.isEmpty(itemPriceString) &&
                mType == ItemEntry.ITEM_TYPE_OTHER &&
                mImageUri == null) {
            Toast.makeText(this, getString(R.string.toast_require_attributes),
                    Toast.LENGTH_SHORT).show();
            itemCanBeSaved = false;
            return itemCanBeSaved;
        }

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values.
        ContentValues values = new ContentValues();
        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.toast_require_picture),
                    Toast.LENGTH_SHORT).show();
            itemCanBeSaved = false;
            return itemCanBeSaved;
        }
        values.put(ItemEntry.COLUMN_ITEM_PICTURE, mImageUri.toString());

        if (TextUtils.isEmpty(itemNameString)) {
            Toast.makeText(this, getString(R.string.toast_require_name),
                    Toast.LENGTH_SHORT).show();
            itemCanBeSaved = false;
            return itemCanBeSaved;
        }
        values.put(ItemEntry.COLUMN_ITEM_NAME, itemNameString);
        values.put(ItemEntry.COLUMN_ITEM_INFORMATION, itemInfoString);
        values.put(ItemEntry.COLUMN_ITEM_TYPE, mType);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, itemSupplierString);

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
                Toast.makeText(this, getString(R.string.error_save_item),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.saved_item),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int mRowsUpdated = getContentResolver().update(
                    mCurrentItemUri,
                    values,
                    null,
                    null);
            if (mRowsUpdated == 0) {
                Toast.makeText(this, getString(R.string.error_update_item) + mRowsUpdated,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.updated_item),
                        Toast.LENGTH_SHORT).show();
            }
        }
        itemCanBeSaved = true;
        return itemCanBeSaved;
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
                if (saveNewItem()) {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Delete the Item
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked "Discard" button, navigate to parent activity;
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog,Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.discard_changes_dialog_msg));
        builder.setPositiveButton(getString(R.string.discard), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.keep_editing),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_dialog_msg));
        builder.setPositiveButton(getString(R.string.delete),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem();
                    }
                });
        builder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked the "Cancel" button, so dismiss the dialog
                        // and continue editing the pet.
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete function if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the item that is being deleted
            int rowsDeleted = getContentResolver().delete(
                    mCurrentItemUri,
                    null,
                    null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "Now is onCreateLoader called");
        // Define a projection that contains all columns from the items table
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_PICTURE,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_TYPE,
                ItemEntry.COLUMN_ITEM_INFORMATION,
                ItemEntry.COLUMN_ITEM_SUPPLIER,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.v(LOG_TAG, "Now is onLoadFinished called");
        // Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that are relevant
            int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PICTURE);
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int infoColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_INFORMATION);
            int typeColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_TYPE);
            int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

            // Extract out the value from the cursor for the given column index
            String imageUriString = cursor.getString(imageColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String info = cursor.getString(infoColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);

            // Update the views on the screen with the values from the database
            mImageUri = Uri.parse(imageUriString);
            mItemImage.setImageURI(mImageUri);
            mItemName.setText(name);
            mItemInfo.setText(info);
            mItemSupplier.setText(supplier);
            mItemPrice.setText(Double.toString(price));
            mEditQuantity.setText(Integer.toString(quantity));

            switch (type) {
                case ItemEntry.ITEM_TYPE_SOFAS:
                    mItemTypeSpinner.setSelection(1);
                    break;
                case ItemEntry.ITEM_TYPE_CHAIRS:
                    mItemTypeSpinner.setSelection(2);
                    break;
                case ItemEntry.ITEM_TYPE_TABLES:
                    mItemTypeSpinner.setSelection(3);
                    break;
                case ItemEntry.ITEM_TYPE_BEDS:
                    mItemTypeSpinner.setSelection(4);
                    break;
                case ItemEntry.ITEM_TYPE_DESKS:
                    mItemTypeSpinner.setSelection(5);
                    break;
                case ItemEntry.ITEM_TYPE_CABINETS:
                    mItemTypeSpinner.setSelection(6);
                    break;
                case ItemEntry.ITEM_TYPE_WARDROBES:
                    mItemTypeSpinner.setSelection(7);
                    break;
                case ItemEntry.ITEM_TYPE_TEXTILES:
                    mItemTypeSpinner.setSelection(8);
                    break;
                case ItemEntry.ITEM_TYPE_DECORATION:
                    mItemTypeSpinner.setSelection(9);
                    break;
                default:
                    mItemTypeSpinner.setSelection(0);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "Now is onLoaderReset called");
        mItemName.setText("");
        mItemInfo.setText("");
        mItemSupplier.setText("");
        mItemPrice.setText("");
        mEditQuantity.setText("");
        mItemTypeSpinner.setSelection(0);
    }
}
