package com.kinvey.android.lists;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kinvey.KCSClient;
import com.kinvey.android.lists.entities.ListEntity;
import com.kinvey.util.ScalarCallback;


public class AddEditListActivity extends Activity {

    private static final String TAG = "AddEditListActivity";
    private KCSClient sharedClient;

    private EditText listName;
    private Button saveButton;
    private Button cancelButton;

    private boolean isEditing = false;
    private String incomingListEntityId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addeditlist);

        try {
            sharedClient = ((KinveyListsApp) getApplication()).getKinveyService();

            listName = (EditText) findViewById(R.id.newlistname);
            saveButton = (Button) findViewById(R.id.save_new_list);
            cancelButton = (Button) findViewById(R.id.cancel_new_list);

            saveButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Clicked Save");
                    if (!isEditing) {
                        createNewList();
                    } else {
                        editList();
                    }

                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Canceling activity...");
                    AddEditListActivity.this.onBackPressed();
                }
            });

            initialize();

        } catch (RuntimeException re) {
            Log.e(TAG, "Error in onCreate", re);
            displayError(re);
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

        if ("new".equals(action)) {
            setTitle(R.string.new_list);
            listName.setText("");
            isEditing = false;
        } else if ("edit".equals(action)) {
            setTitle(R.string.edit_list);
            isEditing = true;
            incomingListEntityId = intent.getStringExtra("listId");
            listName.setText(intent.getStringExtra("listName"));
            listName.selectAll();
        }
    }

    protected void editList() {
        String listNameText = listName.getText().toString();
        if (null == listNameText || listNameText.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter a value", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, String.format("About to save edited list: [%s, %s]", incomingListEntityId, listNameText));
        save(new ListEntity(incomingListEntityId, listNameText));

    }

    protected void createNewList() {
        String listNameText = listName.getText().toString();
        if (null == listNameText || listNameText.equals("")) {
            Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.v(TAG, "Creating new list with name: " + listNameText);
        save(new ListEntity(listNameText));

    }

    private void save(ListEntity item) {
        final ProgressDialog pd = ProgressDialog.show(AddEditListActivity.this, "", "Loading. Please wait...", true);
        sharedClient.mappeddata(MainActivity.LIST_COLLECTION).save(item, new ScalarCallback<Void>() {

        	@Override
        	public void onFailure(Throwable e) {
        		pd.dismiss();
        		Toast.makeText(AddEditListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        		AddEditListActivity.this.onBackPressed();
        	}
        	
            @Override
            public void onSuccess(Void r) {
                pd.dismiss();
                Toast.makeText(AddEditListActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                AddEditListActivity.this.onBackPressed();

            }
        });
    }

    private void displayError(Throwable error) {
        Log.e(TAG, "Unexpected error occurred", error);
        TextView tv = new TextView(this);
        tv.setText(error.toString() + " - see the logs for more details");
        setContentView(tv);
    }
}
