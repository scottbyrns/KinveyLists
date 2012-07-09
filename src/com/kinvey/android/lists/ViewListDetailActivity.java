package com.kinvey.android.lists;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kinvey.KCSClient;
import com.kinvey.MappedAppdata;
import com.kinvey.android.lists.entities.ListItemEntity;
import com.kinvey.util.ListCallback;
import com.kinvey.util.ScalarCallback;

public class ViewListDetailActivity extends Activity {
    private static final String TAG = "ListDetailActivity";

    private KCSClient sharedClient;
    private ListView listItemView;
    private Button addItemButton;

    private String ownerListId;

    private List<ListItemEntity> listItems;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Activity State: onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.listitems);
        try {
            sharedClient = ((KinveyListsApp) getApplication()).getKinveyService();
            listItemView = (ListView) findViewById(R.id.listItems);
            addItemButton = (Button) findViewById(R.id.new_item_button);

            addItemButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ViewListDetailActivity.this, AddEditListItemActivity.class);
                    intent.putExtra("action", "new");
                    intent.putExtra("incomingOwnerId", ownerListId);
                    startActivity(intent);

                }
            });

            listItemView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    ListItemEntity sel = (ListItemEntity) adapterView.getItemAtPosition(position);

                    Intent intent = new Intent(ViewListDetailActivity.this, AddEditListItemActivity.class);
                    intent.putExtra("action", "edit");
                    intent.putExtra("incomingOwnerId", ownerListId);
                    intent.putExtra("incomingItemId", sel.getId());
                    intent.putExtra("itemName", sel.getName());
                    intent.putExtra("itemDesc", sel.getDesc());
                    intent.putExtra("dueDate", sel.getDue());
                    startActivity(intent);
                }
            });

            registerForContextMenu(listItemView);

        } catch (RuntimeException re) {
            Log.e(TAG, "Error occurred in onCreate()", re);
            displayError(re);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ownerListId = getIntent().getExtras().getString("listId");
        Log.d(TAG, "Obtained list Id from intent: " + ownerListId);
        populateListDetailView();
    }

    private void populateListDetailView() {
        Log.i(TAG, "Populating list detail view from KinveyService");
        final ProgressDialog pd = ProgressDialog.show(this, "", "Loading...", true);
        MappedAppdata coll = sharedClient.mappeddata(MainActivity.LIST_ITEM_COLLECTION);

        coll.addFilterCriteria("owner", "==", ownerListId);
        Log.d(TAG, "After adding filter criteria");
        coll.fetchByFilterCriteria(ListItemEntity.class, new ListCallback<ListItemEntity>() {

            @Override
            public void onSuccess(List<ListItemEntity> result) {
                Log.d(TAG, "Received success response");
                listItems = result;
                listItemView.setAdapter(new ArrayAdapter<ListItemEntity>(ViewListDetailActivity.this,
                        android.R.layout.simple_list_item_1, result));
                pd.dismiss();
                Toast.makeText(ViewListDetailActivity.this, "Successful retrieval!", Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "Received error response", error);
                displayError(error);
                pd.dismiss();
            }

        });

    }

    private void displayError(Throwable error) {

        Log.e(TAG, "Unexpected error occurred", error);
        TextView tv = new TextView(this);
        tv.setText(error.toString() + " - see the logs for more details");
        setContentView(tv);

    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View,
     * android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listItems) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(listItems.get(info.position).toString());
            String[] menuItems = getResources().getStringArray(R.array.list_context_menu_items);
            for (int i = 0; i < menuItems.length; i++) {
                if ("Delete".equals(menuItems[i])) {
                    menu.add(Menu.NONE, i, i, menuItems[i]);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemId = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.list_context_menu_items);
        String action = menuItems[menuItemId];
        ListItemEntity activeEntity = listItems.get(((AdapterContextMenuInfo) item.getMenuInfo()).position);
        if ("Delete".equals(action)) {
            deleteListItem(activeEntity);
        } else {
            Log.e(TAG, "Invalid context menu item selected!!!");
        }

        return true;
    }

    private void deleteListItem(ListItemEntity activeEntity) {
        Log.i(TAG, "Attempting to delete list entity: " + activeEntity.toString());
        final ProgressDialog pd = ProgressDialog.show(this, "Deleting", "Please wait...", true);

        final String listItemId = activeEntity.getId();
        sharedClient.mappeddata(MainActivity.LIST_ITEM_COLLECTION).delete(listItemId, new ScalarCallback<Void>() {

            @Override
            public void onSuccess(Void r) {
                pd.dismiss();
                Toast.makeText(ViewListDetailActivity.this, "Item deleted!", Toast.LENGTH_SHORT).show();
                populateListDetailView();

            }
        });

    }

}
