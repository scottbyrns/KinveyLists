/**
 * 
 */
package com.kinvey.android.lists;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kinvey.KCSClient;
import com.kinvey.android.lists.entities.ListItemEntity;
import com.kinvey.util.ScalarCallback;

public class AddEditListItemActivity extends Activity {

    private static final String TAG = "AddEditListItemActivity";
    private KCSClient sharedClient;

    private EditText listItemName;
    private EditText listItemDesc;
    private DatePicker dueDatePicker;
    private Button saveButton;
    private Button cancelButton;

    private boolean isEditing = false;
    private String incomingOwnerId;
    private String incomingItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_edit_list_item);
        try {

            sharedClient = ((KinveyListsApp) getApplication()).getKinveyService();

            listItemName = (EditText) findViewById(R.id.list_item_name_edittext);
            listItemDesc = (EditText) findViewById(R.id.list_item_desc_edittext);
            dueDatePicker = (DatePicker) findViewById(R.id.list_item_due_datepicker);
            saveButton = (Button) findViewById(R.id.list_item_save_button);
            cancelButton = (Button) findViewById(R.id.list_item_cancel_button);

            saveButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Clicked Save");
                    Log.d(TAG,
                            String.format("Date value selected: %d-%d-%d", dueDatePicker.getYear(),
                                    dueDatePicker.getMonth(), dueDatePicker.getDayOfMonth()));
                    if (!isEditing) {
                        createNewListItem();
                    } else {
                        editListItem();
                    }

                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Canceling activity...");
                    AddEditListItemActivity.this.onBackPressed();
                }
            });

        } catch (RuntimeException e) {
            Log.e(TAG, "Error in onCreate", e);
            displayError(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialize();
    }

    /**
     * Common initialization code when the activity is created/resumed
     */
    private void initialize() {
        Intent intent = getIntent();
        String action = (String) intent.getStringExtra("action");
        incomingOwnerId = intent.getStringExtra("incomingOwnerId");

        if ("new".equals(action)) {
            listItemName.setText("");
            listItemDesc.setText("");
            setTitle(R.string.new_item);
            isEditing = false;
            Calendar cal = Calendar.getInstance();

            dueDatePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
        } else if ("edit".equals(action)) {
            setTitle(R.string.edit_item);
            isEditing = true;

            incomingItemId = intent.getStringExtra("incomingItemId");
            listItemName.setText(intent.getStringExtra("itemName"));
            listItemDesc.setText(intent.getStringExtra("itemDesc"));
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date d = df.parse(intent.getStringExtra("dueDate"));
                Log.d(TAG, "Date value is: " + d);
                dueDatePicker.init(1900 + d.getYear(), d.getMonth(), d.getDate(), null);
            } catch (ParseException e) {
                Log.e(TAG, String.format("Error parsing date: %s", intent.getStringExtra("dueDate")), e);
                displayError(e);
            }

        }

    }

    private void save(ListItemEntity listItemEntity) {
        final ProgressDialog pd = ProgressDialog.show(this, "", "Saving item...", true);
        sharedClient.mappeddata(MainActivity.LIST_ITEM_COLLECTION).save(listItemEntity, new ScalarCallback<Void>() {

        	@Override
        	public void onFailure(Throwable e) {
        		pd.dismiss();
        		Toast.makeText(AddEditListItemActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                AddEditListItemActivity.this.onBackPressed();
        	}
        	
            @Override
            public void onSuccess(Void r) {
                pd.dismiss();
                Toast.makeText(AddEditListItemActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                AddEditListItemActivity.this.onBackPressed();

            }
        });

    }

    protected void editListItem() {
        String itemNameText = listItemName.getText().toString();
        String itemDescText = listItemDesc.getText().toString();
        String itemDueDate = String.format("%d-%d-%d", dueDatePicker.getYear(), 1 + dueDatePicker.getMonth(),
                dueDatePicker.getDayOfMonth());

        if (null == itemNameText || itemNameText.equals("")) {
            Toast.makeText(this, "Item name cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.v(TAG, String.format("Editing list item with id [%s]", incomingItemId));
        save(new ListItemEntity(incomingItemId, itemNameText, itemDescText, incomingOwnerId, itemDueDate));

    }

    protected void createNewListItem() {
        String itemNameText = listItemName.getText().toString();
        String itemDescText = listItemDesc.getText().toString();
        String itemDueDate = String.format("%d-%d-%d", dueDatePicker.getYear(), 1 + dueDatePicker.getMonth(),
                dueDatePicker.getDayOfMonth());

        if (null == itemNameText || itemNameText.equals("")) {
            Toast.makeText(this, "Item name cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.v(TAG, String.format("Creating new list item with name: [%s]", itemNameText));
        save(new ListItemEntity(itemNameText, itemDescText, incomingOwnerId, itemDueDate));

    }

    private void displayError(Throwable error) {
        TextView tv = new TextView(this);
        tv.setText(error.toString() + " - see the logs for more details");
        setContentView(tv);
    }

}
